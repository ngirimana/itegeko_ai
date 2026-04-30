from typing import Any

from app.constants.legal_texts import (
    LEGAL_INFORMATION_DISCLAIMER,
    SOURCE_BACKED_CONFIDENCE,
    UNSUPPORTED_ANSWER_TEXT,
    UNSUPPORTED_CONFIDENCE,
)
from app.config.settings import settings
from app.payloads.legal_payloads import AnswerPayload, AskRequest, AskResponse
from app.repositories.legal_repository_interface import LegalRepositoryInterface
from app.services.embedding_service import EmbeddingService
from app.services.legal_rag_service import LegalRagService


class LegalRagServiceImpl(LegalRagService):
    def __init__(
        self,
        repository: LegalRepositoryInterface,
        embedding_service: EmbeddingService,
    ) -> None:
        self.repository = repository
        self.embedding_service = embedding_service

    def ask(self, request: AskRequest) -> AskResponse:
        embedding = self.embedding_service.embed(request.question)
        sources = self.repository.search_by_vector(embedding, request.categoryId)
        sources = [source for source in sources if self._is_relevant(source)]
        if not sources:
            sources = self.repository.fallback_keyword_search(request.question)

        if not sources:
            return AskResponse(
                supported=False,
                answer=AnswerPayload(
                    answerText=UNSUPPORTED_ANSWER_TEXT,
                    confidenceLevel=UNSUPPORTED_CONFIDENCE,
                ),
                sources=[],
            )

        selected_sources = sources[:3]
        source_text = self._build_source_summary(selected_sources)
        return AskResponse(
            supported=True,
            answer=AnswerPayload(
                answerText=(
                    "Based on retrieved Rwanda legal sources:\n\n"
                    f"{source_text}\n\n"
                    f"{LEGAL_INFORMATION_DISCLAIMER}"
                ),
                confidenceLevel=SOURCE_BACKED_CONFIDENCE,
            ),
            sources=selected_sources,
        )

    def _build_source_summary(self, sources: list[dict[str, Any]]) -> str:
        return "\n\n".join(
            f"{source['document_title']}, {source['article_number']}: {source['article_text'][:260]}"
            for source in sources
        )

    def _is_relevant(self, source: dict[str, Any]) -> bool:
        score = source.get("relevance_score") or 0
        return float(score) >= settings.minimum_relevance_score
