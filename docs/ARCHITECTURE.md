# System Architecture - Itegeko AI

This document provides a detailed deep dive into the architecture of Itegeko AI, a production-ready multi-service platform for Rwanda legal information search and RAG-backed Q&A.

## System Overview

Itegeko AI is built using a microservices architecture to ensure scalability, maintainability, and clear separation of concerns.

```mermaid
graph TD
    Client[Browser/Mobile] --> Frontend[Next.js Frontend]
    
    subgraph "Itegeko Ecosystem"
        Frontend --> LegalService[Legal Service - Spring Boot]
        Frontend --> IdentityService[Identity Service - Spring Boot]
        
        LegalService --> AI[AI Service - FastAPI]
        LegalService --> LegalDB[(Legal DB - PostgreSQL + pgvector)]
        LegalService --> Minio[MinIO - Object Storage]
        
        IdentityService --> IdentityDB[(Identity DB - PostgreSQL)]
        IdentityService --> Keycloak[Keycloak - Auth Server]
        
        Scraper[Law Scraper - Python] --> LegalDB
        Scraper --> AI
    end
```

## Service Deep Dives

### 1. Legal Service (`legal-service`)
The core domain service managing legal content.
- **Language/Framework**: Java / Spring Boot 3.x
- **Responsibilities**:
    - Managing `LegalDocument` and `LegalArticle` entities.
    - Handling document uploads and storage in MinIO.
    - Orchestrating the RAG pipeline by calling `ai-service`.
    - Providing search capabilities using both keyword and vector similarity.
    - Managing database migrations for the legal schema.

### 2. AI Service (`ai-service`)
The intelligence layer of the platform.
- **Language/Framework**: Python / FastAPI
- **Responsibilities**:
    - Generating vector embeddings for legal articles.
    - Managing semantic search logic.
    - Orchestrating Retrieval-Augmented Generation (RAG) for Q&A.
    - Communicating with `legal-service` via internal APIs for data retrieval.
- **Key Note**: This service is stateless and does not connect directly to any database.

### 3. Identity Service (`identity-service`)
Manages users and security audits.
- **Language/Framework**: Java / Spring Boot 3.x
- **Responsibilities**:
    - User profile management.
    - Role-based Access Control (RBAC) synchronization with Keycloak.
    - Audit logging and user activity tracking.
    - Organization and membership management.

### 4. Law Scraper (`law-scraper`)
A specialized tool for data ingestion.
- **Language/Framework**: Python
- **Responsibilities**:
    - Crawling the Official RLRC (Rwanda Law Reform Commission) website.
    - Chunking PDFs into granular legal articles.
    - Hashing content to avoid duplicate imports.
    - Triggering re-indexing in the AI service after imports.

## Data Flows

### RAG Pipeline (Question & Answer)

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant L as Legal Service
    participant A as AI Service
    participant D as Legal DB

    U->>F: Asks a legal question
    F->>L: POST /api/v1/ai/ask
    L->>A: POST /v1/ai/rag/query
    A->>L: GET /internal/api/v1/legal/articles/search (Vector)
    L->>D: pgvector similarity search
    D-->>L: Matching articles
    L-->>A: Context snippets
    A->>A: Generate answer using Context + LLM
    A-->>L: Final Answer
    L-->>F: Response with Answer + Sources
    F-->>U: Display Answer
```

### Ingestion Flow (Scraping & Indexing)

```mermaid
sequenceDiagram
    participant S as Scraper
    participant RLRC as RLRC Website
    participant D as Legal DB
    participant A as AI Service
    participant L as Legal Service

    S->>RLRC: Fetch Law PDFs
    S->>S: Extract text & Chunk into Articles
    S->>D: Save Documents & Articles
    S->>A: POST /v1/legal/index
    A->>L: Fetch non-indexed articles
    L->>D: SELECT * FROM legal_articles
    D-->>L: Articles
    L-->>A: Article list
    A->>A: Generate Embeddings (384-dim)
    A->>L: Update Article Embeddings
    L->>D: UPDATE article_embeddings
```

## Security Model

- **Authentication**: Keycloak manages all user identities and issues JWTs.
- **Authorization**:
    - Frontend uses JWT for session management.
    - Backend services validate JWTs using Keycloak's JWK Set URI.
    - RBAC is enforced at the service level (e.g., `hasRole('ADMIN')`).
- **Internal Security**: Services communicate via internal Docker networks. Sensitive endpoints (like indexing) require an `X-Internal-API-Key`.

## Database Schema

### Legal Schema
- `legal_documents`: Metadata about laws, orders, etc.
- `legal_articles`: The actual text of each article.
- `article_embeddings`: Stores `pgvector` data linked to articles.
- `scraped_legal_documents`: Tracks hashes and sources for the scraper.

### Identity Schema
- `users`: User profiles and preferences.
- `audit_logs`: Detailed tracking of system changes.
- `user_activities`: Tracks user interactions (searches, downloads).
