import unittest
import json

import httpx

from app.repositories.legal_repository import LegalServiceRepository


class LegalServiceRepositoryTest(unittest.TestCase):
    def test_lists_articles_for_indexing_from_legal_service(self):
        repository = LegalServiceRepository()
        repository.client = client_for(
            lambda request: httpx.Response(
                200,
                json={"articles": [{"id": "article-1", "chunkText": "Legal chunk"}]},
            )
        )

        articles = repository.list_articles_for_indexing()

        self.assertEqual(articles, [{"id": "article-1", "chunk_text": "Legal chunk"}])

    def test_batches_embedding_upserts_to_legal_service(self):
        captured = {}

        def handler(request: httpx.Request) -> httpx.Response:
            captured["path"] = request.url.path
            captured["payload"] = json.loads(request.read().decode("utf-8"))
            captured["api_key"] = request.headers.get("X-Internal-API-Key")
            return httpx.Response(200, json={"indexed": 1})

        repository = LegalServiceRepository()
        repository.client = client_for(handler)

        indexed = repository.upsert_embeddings([
            {
                "article_id": "article-1",
                "chunk_text": "Legal chunk",
                "chunk_hash": "hash",
                "embedding": [0.1, 0.2],
            }
        ])

        self.assertEqual(indexed, 1)
        self.assertEqual(captured["path"], "/internal/ai/embeddings")
        self.assertEqual(captured["payload"]["embeddings"][0]["articleId"], "article-1")
        self.assertIsNotNone(captured["api_key"])

    def test_vector_search_returns_legal_service_results(self):
        repository = LegalServiceRepository()
        repository.client = client_for(
            lambda request: httpx.Response(
                200,
                json={"results": [{"article_number": "Article 1", "relevance_score": 0.9}]},
            )
        )

        results = repository.search_by_vector([0.1, 0.2])

        self.assertEqual(results[0]["article_number"], "Article 1")


def client_for(handler):
    return httpx.Client(
        base_url="http://legal-service.test",
        headers={"X-Internal-API-Key": "test-key"},
        transport=httpx.MockTransport(handler),
    )


if __name__ == "__main__":
    unittest.main()
