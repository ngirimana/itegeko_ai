from app.payloads.legal_payloads import IndexResponse
from app.repositories.legal_repository_interface import LegalRepositoryInterface
from app.services.embedding_service import EmbeddingService
from app.services.indexing_service import IndexingService
from app.utils.hashing import sha256_text


class IndexingServiceImpl(IndexingService):
    def __init__(
        self,
        repository: LegalRepositoryInterface,
        embedding_service: EmbeddingService,
    ) -> None:
        self.repository = repository
        self.embedding_service = embedding_service

    def index_articles(self) -> IndexResponse:
        articles = self.repository.list_articles_for_indexing()
        records = []
        for article in articles:
            chunk_text = article["chunk_text"]
            records.append(
                {
                    "article_id": str(article["id"]),
                    "chunk_text": chunk_text,
                    "chunk_hash": sha256_text(chunk_text),
                    "embedding": self.embedding_service.embed(chunk_text),
                }
            )
        return IndexResponse(indexed=self.repository.upsert_embeddings(records))
