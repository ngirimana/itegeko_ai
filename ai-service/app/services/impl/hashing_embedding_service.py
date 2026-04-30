import hashlib
import math

from app.config.settings import settings
from app.services.embedding_service import EmbeddingService


class HashingEmbeddingService(EmbeddingService):
    """Deterministic MVP embedding until a production embedding model is selected."""

    def embed(self, text: str) -> list[float]:
        vector = [0.0] * settings.embedding_dimension
        for token in self._tokens(text):
            digest = hashlib.sha256(token.encode("utf-8")).digest()
            index = int.from_bytes(digest[:4], "big") % settings.embedding_dimension
            sign = 1.0 if digest[4] % 2 == 0 else -1.0
            vector[index] += sign
        norm = math.sqrt(sum(value * value for value in vector)) or 1.0
        return [value / norm for value in vector]

    def _tokens(self, text: str) -> list[str]:
        return [token for token in text.lower().replace("-", " ").split() if len(token) > 1]
