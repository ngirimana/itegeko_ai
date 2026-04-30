package rw.itegeko.legal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import rw.itegeko.legal.payloads.admin.CreateLegalArticleRequest;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentRequest;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentResponse;
import rw.itegeko.legal.services.LegalAdminService;

class LegalAdminControllerTest {
    private final LegalAdminService adminService = mock(LegalAdminService.class);
    private final LegalAdminController controller = new LegalAdminController(adminService);

    @Test
    void delegatesDocumentCreationToAdminService() {
        var request = request();
        var expected = new CreateLegalDocumentResponse(UUID.randomUUID(), List.of(UUID.randomUUID()), 1, "indexed");
        when(adminService.createDocument(request)).thenReturn(expected);

        var response = controller.createDocument(request);

        assertEquals(expected, response);
        verify(adminService).createDocument(request);
    }

    private CreateLegalDocumentRequest request() {
        return new CreateLegalDocumentRequest(
            "New Law",
            "LAW-1",
            java.time.LocalDate.parse("2026-01-01"),
            "English",
            "active",
            "https://example.test/law",
            null,
            null,
            List.of(new CreateLegalArticleRequest("Article 1", "Scope", "This law applies."))
        );
    }
}
