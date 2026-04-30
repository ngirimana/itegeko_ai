from abc import ABC, abstractmethod

from app.payloads.legal_payloads import AskRequest, AskResponse


class LegalRagService(ABC):
    @abstractmethod
    def ask(self, request: AskRequest) -> AskResponse:
        pass
