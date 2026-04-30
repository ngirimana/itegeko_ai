import pytest
from fastapi import FastAPI, HTTPException

from app.api.health_controller import health
from app.api.legal_controller import (
    ask,
    get_indexing_service,
    get_rag_service,
    index_articles,
    require_internal_api_key,
)
from app.config.settings import settings
from app.exceptions.application_exception import ApplicationException
from app.exceptions.handlers import register_exception_handlers
from app.payloads.legal_payloads import AskRequest, AskResponse, IndexResponse
from app.services.embedding_service import EmbeddingService
from app.services.impl.hashing_embedding_service import HashingEmbeddingService
from app.services.indexing_service import IndexingService
from app.services.legal_rag_service import LegalRagService


def test_health_response_and_module_exports():
    assert health().status == "ok"

    from app import embedding, settings as settings_module
    from app.routers import legal as legal_router_module

    assert embedding.HashingEmbeddingService is HashingEmbeddingService
    assert legal_router_module.router is not None
    assert settings_module.settings is settings


def test_hashing_embedding_normalizes_tokens():
    service = HashingEmbeddingService()

    vector = service.embed("Land-law land")

    assert len(vector) == settings.embedding_dimension
    assert pytest.approx(sum(value * value for value in vector), rel=1e-6) == 1.0
    assert service._tokens("A b cd-ef") == ["cd", "ef"]


def test_abstract_service_methods_are_overridden():
    class ConcreteEmbedding(EmbeddingService):
        def embed(self, text: str) -> list[float]:
            return super().embed(text)

    class ConcreteIndexing(IndexingService):
        def index_articles(self) -> IndexResponse:
            return super().index_articles()

    class ConcreteRag(LegalRagService):
        def ask(self, request: AskRequest) -> AskResponse:
            return super().ask(request)

    assert ConcreteEmbedding().embed("text") is None
    assert ConcreteIndexing().index_articles() is None
    assert ConcreteRag().ask(AskRequest(question="Q?", categoryId=None)) is None


def test_legal_controller_dependencies_and_api_key_guard():
    assert get_rag_service() is not None
    assert get_indexing_service() is not None
    require_internal_api_key(settings.internal_api_key)

    with pytest.raises(HTTPException) as exception:
        require_internal_api_key("wrong")

    assert exception.value.status_code == 403


def test_legal_controller_delegates_to_services():
    class Rag(LegalRagService):
        def ask(self, request: AskRequest) -> AskResponse:
            return AskResponse(
                supported=True,
                answer={"answerText": request.question, "confidenceLevel": "high"},
                sources=[],
            )

    class Indexing(IndexingService):
        def index_articles(self) -> IndexResponse:
            return IndexResponse(indexed=3)

    assert ask(AskRequest(question="What is the law?", categoryId=None), Rag()).supported is True
    assert index_articles(None, Indexing()).indexed == 3


@pytest.mark.asyncio
async def test_application_exception_handler_returns_json_response():
    app = FastAPI()
    register_exception_handlers(app)
    handler = app.exception_handlers[ApplicationException]

    response = await handler(None, ApplicationException("Nope", 418))

    assert response.status_code == 418
    assert response.body == b'{"message":"Nope"}'
