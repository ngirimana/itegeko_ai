from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    legal_service_url: str = "http://localhost:8080"
    legal_service_timeout_seconds: float = 10.0
    embedding_dimension: int = 384
    embedding_model: str = "mvp-hashing-embedding-384"
    minimum_relevance_score: float = 0.08
    internal_api_key: str = "local-dev-internal-key"
    cors_allowed_origins: str = "http://localhost:3000"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
