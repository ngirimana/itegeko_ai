import unittest

from app.services.impl.indexing_service_impl import IndexingServiceImpl


class FakeRepository:
    def __init__(self) -> None:
        self.records = []

    def list_articles_for_indexing(self):
        return [
            {"id": "article-1", "chunk_text": "Labour law Article 1"},
            {"id": "article-2", "chunk_text": "Labour law Article 2"},
        ]

    def upsert_embeddings(self, records):
        self.records = records
        return len(records)


class FakeEmbeddingService:
    def embed(self, text: str):
        return [float(len(text))]


class IndexingServiceImplTest(unittest.TestCase):
    def test_index_articles_batches_embedding_writes(self):
        repository = FakeRepository()
        service = IndexingServiceImpl(repository, FakeEmbeddingService())

        response = service.index_articles()

        self.assertEqual(response.indexed, 2)
        self.assertEqual(len(repository.records), 2)
        self.assertEqual(repository.records[0]["article_id"], "article-1")
        self.assertIn("chunk_hash", repository.records[0])
        self.assertEqual(repository.records[0]["embedding"], [20.0])


if __name__ == "__main__":
    unittest.main()
