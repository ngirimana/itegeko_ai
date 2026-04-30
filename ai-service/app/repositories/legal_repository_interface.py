from abc import ABC, abstractmethod
from typing import Any


class LegalRepositoryInterface(ABC):
    @abstractmethod
    def list_articles_for_indexing(self) -> list[dict[str, Any]]:
        pass

    @abstractmethod
    def upsert_embedding(self, article_id: str, chunk_text: str, chunk_hash: str, embedding: list[float]) -> None:
        pass

    def upsert_embeddings(self, records: list[dict[str, Any]]) -> int:
        for record in records:
            self.upsert_embedding(
                record["article_id"],
                record["chunk_text"],
                record["chunk_hash"],
                record["embedding"],
            )
        return len(records)

    @abstractmethod
    def search_by_vector(
        self,
        embedding: list[float],
        category_id: str | None = None,
        limit: int = 5,
    ) -> list[dict[str, Any]]:
        pass

    @abstractmethod
    def fallback_keyword_search(self, query: str, limit: int = 5) -> list[dict[str, Any]]:
        pass
