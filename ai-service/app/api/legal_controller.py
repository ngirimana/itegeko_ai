from typing import Annotated

from fastapi import APIRouter, Depends, Header, HTTPException, status
from app.config.settings import settings
from app.payloads.legal_payloads import AskRequest, AskResponse, IndexResponse
from app.repositories.legal_repository import LegalServiceRepository
from app.services.impl.hashing_embedding_service import HashingEmbeddingService
from app.services.impl.indexing_service_impl import IndexingServiceImpl
from app.services.impl.legal_rag_service_impl import LegalRagServiceImpl
from app.services.indexing_service import IndexingService
from app.services.legal_rag_service import LegalRagService

router = APIRouter()

repository = LegalServiceRepository()
embedding_service = HashingEmbeddingService()
rag_service = LegalRagServiceImpl(repository, embedding_service)
indexing_service = IndexingServiceImpl(repository, embedding_service)


def get_rag_service() -> LegalRagService:
    return rag_service


def get_indexing_service() -> IndexingService:
    return indexing_service


def require_internal_api_key(
    x_internal_api_key: Annotated[str | None, Header(alias="X-Internal-API-Key")] = None,
) -> None:
    if x_internal_api_key != settings.internal_api_key:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Invalid internal API key.")


@router.post("/ask", response_model=AskResponse)
def ask(
    request: AskRequest,
    service: LegalRagService = Depends(get_rag_service),
) -> AskResponse:
    return service.ask(request)


@router.post("/index", response_model=IndexResponse)
def index_articles(
    _: None = Depends(require_internal_api_key),
    service: IndexingService = Depends(get_indexing_service),
) -> IndexResponse:
    return service.index_articles()
