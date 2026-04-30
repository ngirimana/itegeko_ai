package rw.itegeko.legal.services.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.itegeko.legal.payloads.LegalArticleResponse;
import rw.itegeko.legal.payloads.SearchResponse;
import rw.itegeko.legal.repositories.LegalArticleRepository;
import rw.itegeko.legal.services.LegalSearchService;

@Service
public class LegalSearchServiceImpl implements LegalSearchService {
    private final LegalArticleRepository articleRepository;

    public LegalSearchServiceImpl(LegalArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(String query, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var resultsPage = query == null || query.isBlank()
            ? articleRepository.findAllSummaries(pageable)
            : articleRepository.keywordSearch(query.trim(), pageable);
        var results = resultsPage.getContent().stream()
            .map(LegalArticleResponse::from)
            .toList();
        return new SearchResponse(
            results,
            resultsPage.getNumber(),
            resultsPage.getSize(),
            resultsPage.getTotalElements(),
            resultsPage.getTotalPages()
        );
    }
}
