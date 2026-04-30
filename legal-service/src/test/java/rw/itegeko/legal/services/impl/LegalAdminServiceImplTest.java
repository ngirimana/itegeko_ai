package rw.itegeko.legal.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import rw.itegeko.legal.exceptions.ResourceNotFoundException;
import rw.itegeko.legal.exceptions.ServiceUnavailableException;
import rw.itegeko.legal.payloads.admin.CreateLegalArticleRequest;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentRequest;
import rw.itegeko.legal.repositories.CreatedLegalDocument;
import rw.itegeko.legal.repositories.LegalAdminRepository;
import rw.itegeko.legal.repositories.LegalCategoryRepository;
import rw.itegeko.legal.repositories.LegalSourceRepository;
import rw.itegeko.legal.services.AiService;

class LegalAdminServiceImplTest {
    private final LegalAdminRepository adminRepository = mock(LegalAdminRepository.class);
    private final LegalCategoryRepository categoryRepository = mock(LegalCategoryRepository.class);
    private final LegalSourceRepository sourceRepository = mock(LegalSourceRepository.class);
    private final AiService aiService = mock(AiService.class);
    private final LegalAdminServiceImpl service = new LegalAdminServiceImpl(
        adminRepository,
        categoryRepository,
        sourceRepository,
        aiService
    );

    @Test
    void createsDocumentAndIndexesLegalContent() {
        var documentId = UUID.randomUUID();
        var articleId = UUID.randomUUID();
        var request = request(null, null);
        when(adminRepository.createDocument(request)).thenReturn(new CreatedLegalDocument(documentId, List.of(articleId)));
        when(aiService.indexLegalContent()).thenReturn(5);

        var response = service.createDocument(request);

        assertEquals(documentId, response.documentId());
        assertEquals(List.of(articleId), response.articleIds());
        assertEquals(5, response.indexedArticles());
        assertEquals("indexed", response.indexingStatus());
        verify(adminRepository).createDocument(request);
    }

    @Test
    void returnsIndexingFailedWhenAiIndexingIsUnavailable() {
        var documentId = UUID.randomUUID();
        var request = request(null, null);
        when(adminRepository.createDocument(request)).thenReturn(new CreatedLegalDocument(documentId, List.of()));
        when(aiService.indexLegalContent()).thenThrow(new ServiceUnavailableException("AI unavailable", null));

        var response = service.createDocument(request);

        assertEquals(documentId, response.documentId());
        assertEquals(0, response.indexedArticles());
        assertEquals("indexing_failed", response.indexingStatus());
    }

    @Test
    void rejectsUnknownCategory() {
        var categoryId = UUID.randomUUID();
        var request = request(categoryId, null);
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.createDocument(request));
    }

    @Test
    void rejectsUnknownSource() {
        var sourceId = UUID.randomUUID();
        var request = request(null, sourceId);
        when(sourceRepository.existsById(sourceId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.createDocument(request));
    }

    private CreateLegalDocumentRequest request(UUID categoryId, UUID sourceId) {
        return new CreateLegalDocumentRequest(
            "New Law",
            "LAW-1",
            java.time.LocalDate.parse("2026-01-01"),
            "English",
            "active",
            "https://example.test/law",
            categoryId,
            sourceId,
            List.of(new CreateLegalArticleRequest("Article 1", "Scope", "This law applies."))
        );
    }
}
