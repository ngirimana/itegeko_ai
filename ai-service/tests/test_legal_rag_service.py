import unittest

from app.constants.legal_texts import UNSUPPORTED_ANSWER_TEXT
from app.services.impl.legal_rag_service_impl import LegalRagServiceImpl
from app.payloads.legal_payloads import AskRequest


class FakeEmbeddingService:
    def embed(self, text: str):
        return [1.0, 0.0, 0.0]


class FakeRepository:
    def __init__(self, vector_sources=None, fallback_sources=None):
        self.vector_sources = vector_sources or []
        self.fallback_sources = fallback_sources or []
        self.fallback_called = False

    def search_by_vector(self, embedding, category_id=None, limit=5):
        return self.vector_sources

    def fallback_keyword_search(self, query, limit=5):
        self.fallback_called = True
        return self.fallback_sources


class LegalRagServiceImplTest(unittest.TestCase):
    def test_returns_unsupported_when_no_sources_are_found(self):
        service = LegalRagServiceImpl(FakeRepository(), FakeEmbeddingService())

        response = service.ask(AskRequest(question="unknown question"))

        self.assertFalse(response.supported)
        self.assertEqual(response.answer.answerText, UNSUPPORTED_ANSWER_TEXT)
        self.assertEqual(response.sources, [])

    def test_filters_low_relevance_vector_sources_and_uses_keyword_fallback(self):
        fallback_source = source("Labour Law", "Article 2", "Annual leave source text.", 0.25)
        repository = FakeRepository(
            vector_sources=[source("Weak Match", "Article 1", "Weak source text.", 0.01)],
            fallback_sources=[fallback_source],
        )
        service = LegalRagServiceImpl(repository, FakeEmbeddingService())

        response = service.ask(AskRequest(question="annual leave"))

        self.assertTrue(repository.fallback_called)
        self.assertTrue(response.supported)
        self.assertEqual(response.sources, [fallback_source])
        self.assertIn("Annual leave source text.", response.answer.answerText)

    def test_uses_high_relevance_vector_sources(self):
        vector_source = source("Labour Law", "Article 3", "Notice period source text.", 0.8)
        repository = FakeRepository(vector_sources=[vector_source])
        service = LegalRagServiceImpl(repository, FakeEmbeddingService())

        response = service.ask(AskRequest(question="notice period"))

        self.assertFalse(repository.fallback_called)
        self.assertTrue(response.supported)
        self.assertEqual(response.sources, [vector_source])
        self.assertIn("Notice period source text.", response.answer.answerText)


def source(document_title: str, article_number: str, article_text: str, relevance_score: float):
    return {
        "document_title": document_title,
        "article_number": article_number,
        "article_text": article_text,
        "relevance_score": relevance_score,
    }


if __name__ == "__main__":
    unittest.main()
