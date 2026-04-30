package rw.itegeko.legal.services.impl;

import org.springframework.stereotype.Service;
import rw.itegeko.legal.payloads.CatalogResponse;
import rw.itegeko.legal.payloads.LegalCategoryResponse;
import rw.itegeko.legal.payloads.LegalSourceResponse;
import rw.itegeko.legal.repositories.LegalCategoryRepository;
import rw.itegeko.legal.repositories.LegalSourceRepository;
import rw.itegeko.legal.services.LegalCatalogService;

@Service
public class LegalCatalogServiceImpl implements LegalCatalogService {
    private final LegalCategoryRepository categoryRepository;
    private final LegalSourceRepository sourceRepository;

    public LegalCatalogServiceImpl(
        LegalCategoryRepository categoryRepository,
        LegalSourceRepository sourceRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.sourceRepository = sourceRepository;
    }

    @Override
    public CatalogResponse getCatalog() {
        var categories = categoryRepository.findAll().stream()
            .map(LegalCategoryResponse::from)
            .toList();
        var sources = sourceRepository.findAll().stream()
            .map(LegalSourceResponse::from)
            .toList();
        return new CatalogResponse(categories, sources);
    }
}
