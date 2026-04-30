from fastapi import APIRouter

from app.constants.api_paths import HEALTH_PATH
from app.payloads.legal_payloads import HealthResponse

router = APIRouter()


@router.get(HEALTH_PATH, response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok")
