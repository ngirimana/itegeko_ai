from typing import Any

import httpx

from app.config.settings import settings
from app.repositories.legal_repository_interface import LegalRepositoryInterface


class LegalServiceRepository(LegalRepositoryInterface):
    """HTTP adapter for legal data owned by legal-service."""

    def __init__(self) -> None:
        self.client = httpx.Client(
            base_url=settings.legal_service_url.rstrip("/"),
            timeout=settings.legal_service_timeout_seconds,
            headers={"X-Internal-API-Key": settings.internal_api_key},
        )

    def list_articles_for_indexing(self) -> list[dict[str, Any]]:
        response = self.client.get("/internal/ai/articles-for-indexing")
        response.raise_for_status()
        articles = response.json().get("articles", [])
        return [
            {
                "id": article["id"],
                "chunk_text": article["chunkText"],
            }
            for article in articles
        ]

    def upsert_embedding(self, article_id: str, chunk_text: str, chunk_hash: str, embedding: list[float]) -> None:
        self.upsert_embeddings([
            {
                "article_id": article_id,
                "chunk_text": chunk_text,
                "chunk_hash": chunk_hash,
                "embedding": embedding,
            }
        ])

    def upsert_embeddings(self, records: list[dict[str, Any]]) -> int:
        payload = {
            "embeddings": [
                {
                    "articleId": record["article_id"],
                    "embeddingModel": settings.embedding_model,
                    "embedding": record["embedding"],
                    "chunkText": record["chunk_text"],
                    "chunkHash": record["chunk_hash"],
                }
                for record in records
            ]
        }
        response = self.client.post("/internal/ai/embeddings", json=payload)
        response.raise_for_status()
        return int(response.json().get("indexed", 0))

    def search_by_vector(
        self,
        embedding: list[float],
        category_id: str | None = None,
        limit: int = 5,
    ) -> list[dict[str, Any]]:
        response = self.client.post(
            "/internal/ai/vector-search",
            json={
                "embedding": embedding,
                "categoryId": category_id,
                "limit": limit,
            },
        )
        response.raise_for_status()
        return response.json().get("results", [])

    def fallback_keyword_search(self, query: str, limit: int = 5) -> list[dict[str, Any]]:
        response = self.client.post(
            "/internal/ai/keyword-search",
            json={
                "query": query,
                "limit": limit,
            },
        )
        response.raise_for_status()
        return response.json().get("results", [])


LegalRepository = LegalServiceRepository
