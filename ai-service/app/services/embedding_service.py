from abc import ABC, abstractmethod


class EmbeddingService(ABC):
    @abstractmethod
    def embed(self, text: str) -> list[float]:
        pass
