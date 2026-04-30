from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.health_controller import router as health_router
from app.api.legal_controller import router as legal_router
from app.config.settings import settings
from app.constants.api_paths import API_PREFIX_LEGAL
from app.exceptions.handlers import register_exception_handlers

allowed_origins = [origin.strip() for origin in settings.cors_allowed_origins.split(",") if origin.strip()]

app = FastAPI(
    title="Itegeko AI RAG Service",
    version="0.1.0",
    description="FastAPI service for legal retrieval, embeddings, and source-backed answers.",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

register_exception_handlers(app)
app.include_router(health_router, tags=["health"])
app.include_router(legal_router, prefix=API_PREFIX_LEGAL, tags=["legal-rag"])
