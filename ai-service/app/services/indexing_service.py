from abc import ABC, abstractmethod

from app.payloads.legal_payloads import IndexResponse


class IndexingService(ABC):
    @abstractmethod
    def index_articles(self) -> IndexResponse:
        pass
