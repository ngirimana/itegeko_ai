from typing import Any

from pydantic import BaseModel, Field


class AskRequest(BaseModel):
    question: str = Field(min_length=2, max_length=1000)
    categoryId: str | None = None


class AnswerPayload(BaseModel):
    answerText: str
    confidenceLevel: str


class AskResponse(BaseModel):
    supported: bool
    answer: AnswerPayload
    sources: list[dict[str, Any]]


class IndexResponse(BaseModel):
    indexed: int


class HealthResponse(BaseModel):
    status: str
