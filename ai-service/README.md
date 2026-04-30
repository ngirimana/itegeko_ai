# 🤖 Itegeko AI Service

The **AI Service** is the intelligence engine behind Itegeko AI. It handles embedding generation, semantic search orchestration, and Retrieval-Augmented Generation (RAG). It is designed to be a stateless service that interfaces with the `legal-service` for data persistence.

## 🚀 Tech Stack

- **Framework**: FastAPI
- **Language**: Python 3.11+
- **Machine Learning**: NumPy (for vector operations), Pydantic (data validation)
- **Networking**: HTTPX (for internal API calls)
- **Production Server**: Uvicorn

## 📂 Project Structure

```text
app/
  ├── api/           # FastAPI routers and endpoints
  ├── config/        # Environment settings (Pydantic Settings)
  ├── constants/     # Fixed legal texts and API paths
  ├── exceptions/    # Custom application exceptions
  ├── payloads/      # Request and response models
  ├── repositories/  # HTTP adapters for legal-service communication
  ├── services/      # Business logic (RAG, Indexing, Embeddings)
  └── utils/         # Helper functions (hashing, text cleaning)
```

## 🧠 AI Capabilities

- **Embeddings**: Generates 384-dimension vectors for legal articles. The current implementation uses a deterministic hashing-based MVP model, designed to be swapped with heavy transformers like `sentence-transformers`.
- **RAG Orchestration**: Combines user queries with retrieved context to generate informed legal answers.
- **Internal Retrieval**: Calls `legal-service` internal APIs to find the best context matches from `pgvector`.

## 🛠️ Key Endpoints

- `POST /v1/ai/rag/query`: Primary RAG entry point.
- `POST /v1/legal/index`: (Internal) Triggers indexing/re-indexing of legal articles.
- `GET /health`: Basic health check.

## 🧪 Development Setup

1. **Create Virtual Environment**:
   ```bash
   python3 -m venv .venv
   source .venv/bin/activate
   ```

2. **Install Dependencies**:
   ```bash
   pip install -r requirements.txt
   pip install -r requirements-dev.txt
   ```

3. **Run Locally**:
   ```bash
   uvicorn app.main:app --reload
   ```

## 🧪 Testing

```bash
pytest
```
*Note: Some tests may require `scripts/test-ai.sh` for full integration checks.*
