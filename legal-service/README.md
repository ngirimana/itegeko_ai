# ⚖️ Itegeko Legal Service

The **Legal Service** is the core backend component of Itegeko AI. It manages the legal domain data, including documents, articles, and their metadata. It also orchestrates the RAG pipeline by interfacing with the AI Service and manages persistent storage for legal PDFs.

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.3.7
- **Language**: Java 21
- **Database**: PostgreSQL with `pgvector`
- **Migrations**: Flyway
- **Object Storage**: MinIO (S3-compatible)
- **Security**: Spring Security + OAuth2 Resource Server (Keycloak)
- **Communication**: WebClient (for AI Service interaction)

## 📂 Internal Structure

```text
src/main/java/rw/itegeko/legal/
  ├── config/        # Security, CORS, MinIO, and WebClient configs
  ├── constants/     # Reusable strings and API paths
  ├── controllers/   # REST Endpoints (Public & Internal)
  ├── entities/      # JPA entities (Document, Article, Embedding)
  ├── exceptions/    # Global exception handling
  ├── payloads/      # Request/Response DTOs
  ├── repositories/  # Spring Data JPA repositories
  └── services/      # Business logic & AI Service orchestration
```

## 🔐 Security & Internal APIs

This service provides both public APIs for the frontend and internal APIs for the `ai-service` and `law-scraper`.
- **Public APIs**: Protected via JWT from Keycloak.
- **Internal APIs**: Protected via `X-Internal-API-Key` header. These endpoints allow the AI service to fetch articles for indexing and update vector embeddings.

## 🛠️ Key Endpoints

- `GET /api/v1/legal/search`: Semantic and keyword search for laws.
- `POST /api/v1/ai/ask`: RAG-powered legal Q&A.
- `GET /api/v1/legal/documents/{id}`: Fetch document metadata and articles.
- `POST /internal/api/v1/legal/embeddings`: (Internal) Update article vector embeddings.

## 🧪 Running Tests

```bash
mvn test
```

## 🏗️ Building & Running

**Docker (Recommended)**:
```bash
docker compose up legal-service --build
```

**Local**:
Make sure you have a PostgreSQL instance and MinIO running.
```bash
mvn spring-boot:run
```
