package rw.itegeko.identity.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsKnownExceptionsToApiErrors() {
        var notFound = handler.handleNotFound(new ResourceNotFoundException("Missing"));
        var denied = handler.handleAccessDenied(new AccessDeniedException("Denied"));

        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", notFound.getBody().code());
        assertEquals("Missing", notFound.getBody().message());
        assertEquals(HttpStatus.FORBIDDEN, denied.getStatusCode());
        assertEquals("ACCESS_DENIED", denied.getBody().code());
    }

    @Test
    void mapsValidationAndUnexpectedErrorsToStableMessages() {
        var validation = handler.handleValidation(null);
        var unexpected = handler.handleUnexpected(new RuntimeException("boom"));

        assertEquals(HttpStatus.BAD_REQUEST, validation.getStatusCode());
        assertEquals("VALIDATION_ERROR", validation.getBody().code());
        assertEquals("Request validation failed.", validation.getBody().message());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unexpected.getStatusCode());
        assertEquals("INTERNAL_ERROR", unexpected.getBody().code());
        assertEquals("Unexpected server error.", unexpected.getBody().message());
    }
}
