package rw.itegeko.legal.services.impl;

import org.springframework.stereotype.Service;
import rw.itegeko.legal.exceptions.ResourceNotFoundException;
import rw.itegeko.legal.exceptions.ServiceUnavailableException;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentRequest;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentResponse;
import rw.itegeko.legal.repositories.LegalAdminRepository;
import rw.itegeko.legal.repositories.LegalCategoryRepository;
import rw.itegeko.legal.repositories.LegalSourceRepository;
import rw.itegeko.legal.services.AiService;
import rw.itegeko.legal.services.LegalAdminService;

@Service
public class LegalAdminServiceImpl implements LegalAdminService {
    private static final String INDEXED = "indexed";
    private static final String INDEXING_FAILED = "indexing_failed";

    private final LegalAdminRepository adminRepository;
    private final LegalCategoryRepository categoryRepository;
    private final LegalSourceRepository sourceRepository;
    private final AiService aiService;

    public LegalAdminServiceImpl(
        LegalAdminRepository adminRepository,
        LegalCategoryRepository categoryRepository,
        LegalSourceRepository sourceRepository,
        AiService aiService
    ) {
        this.adminRepository = adminRepository;
        this.categoryRepository = categoryRepository;
        this.sourceRepository = sourceRepository;
        this.aiService = aiService;
    }

    @Override
    public CreateLegalDocumentResponse createDocument(CreateLegalDocumentRequest request) {
        validateReferences(request);
        var created = adminRepository.createDocument(request);
        var indexedArticles = 0;
        var indexingStatus = INDEXED;
        try {
            indexedArticles = aiService.indexLegalContent();
        } catch (ServiceUnavailableException exception) {
            indexingStatus = INDEXING_FAILED;
        }
        return new CreateLegalDocumentResponse(
            created.documentId(),
            created.articleIds(),
            indexedArticles,
            indexingStatus
        );
    }

    private void validateReferences(CreateLegalDocumentRequest request) {
        if (request.categoryId() != null && !categoryRepository.existsById(request.categoryId())) {
            throw new ResourceNotFoundException("Legal category was not found.");
        }
        if (request.sourceId() != null && !sourceRepository.existsById(request.sourceId())) {
            throw new ResourceNotFoundException("Legal source was not found.");
        }
    }
}
