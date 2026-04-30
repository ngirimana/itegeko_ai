import unittest

from fastapi.testclient import TestClient

from app.api.legal_controller import get_indexing_service, get_rag_service
from app.config.settings import settings
from app.main import app
from app.payloads.legal_payloads import AnswerPayload, AskResponse, IndexResponse


class FakeRagService:
    def ask(self, request):
        return AskResponse(
            supported=True,
            answer=AnswerPayload(answerText=f"Answer for {request.question}", confidenceLevel="source-backed"),
            sources=[{"article_number": "Article 1"}],
        )


class FakeIndexingService:
    def index_articles(self):
        return IndexResponse(indexed=3)


class LegalControllerTest(unittest.TestCase):
    def setUp(self):
        app.dependency_overrides[get_rag_service] = lambda: FakeRagService()
        app.dependency_overrides[get_indexing_service] = lambda: FakeIndexingService()
        self.client = TestClient(app)

    def tearDown(self):
        app.dependency_overrides.clear()

    def test_ask_endpoint_returns_rag_response(self):
        response = self.client.post("/v1/legal/ask", json={"question": "What is annual leave?"})

        self.assertEqual(response.status_code, 200)
        payload = response.json()
        self.assertTrue(payload["supported"])
        self.assertEqual(payload["answer"]["answerText"], "Answer for What is annual leave?")
        self.assertEqual(payload["sources"][0]["article_number"], "Article 1")

    def test_index_endpoint_requires_internal_api_key(self):
        response = self.client.post("/v1/legal/index")

        self.assertEqual(response.status_code, 403)

    def test_index_endpoint_accepts_internal_api_key(self):
        response = self.client.post(
            "/v1/legal/index",
            headers={"X-Internal-API-Key": settings.internal_api_key},
        )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {"indexed": 3})


if __name__ == "__main__":
    unittest.main()
