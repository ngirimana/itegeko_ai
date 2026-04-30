package rw.itegeko.legal.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.itegeko.legal.payloads.internal.ArticlesForIndexingResponse;
import rw.itegeko.legal.payloads.internal.EmbeddingBatchUpsertRequest;
import rw.itegeko.legal.payloads.internal.IndexResponse;
import rw.itegeko.legal.payloads.internal.KeywordSearchRequest;
import rw.itegeko.legal.payloads.internal.LegalRetrievalResponse;
import rw.itegeko.legal.payloads.internal.VectorSearchRequest;
import rw.itegeko.legal.repositories.LegalAiDataRepository;
import rw.itegeko.legal.services.InternalAiDataService;

@Service
public class InternalAiDataServiceImpl implements InternalAiDataService {
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final LegalAiDataRepository repository;

    public InternalAiDataServiceImpl(LegalAiDataRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public ArticlesForIndexingResponse listArticlesForIndexing() {
        return new ArticlesForIndexingResponse(repository.listArticlesForIndexing());
    }

    @Override
    @Transactional
    public IndexResponse upsertEmbeddings(EmbeddingBatchUpsertRequest request) {
        return new IndexResponse(repository.upsertEmbeddings(request.embeddings()));
    }

    @Override
    @Transactional(readOnly = true)
    public LegalRetrievalResponse searchByVector(VectorSearchRequest request) {
        return new LegalRetrievalResponse(repository.searchByVector(
            request.embedding(),
            request.categoryId(),
            normalizeLimit(request.limit())
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public LegalRetrievalResponse fallbackKeywordSearch(KeywordSearchRequest request) {
        return new LegalRetrievalResponse(repository.fallbackKeywordSearch(
            request.query() == null ? "" : request.query(),
            normalizeLimit(request.limit())
        ));
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
