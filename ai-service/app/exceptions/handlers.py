from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.exceptions.application_exception import ApplicationException


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(ApplicationException)
    async def handle_application_exception(_: Request, exception: ApplicationException) -> JSONResponse:
        return JSONResponse(
            status_code=exception.status_code,
            content={"message": exception.message},
        )
