package rw.itegeko.legal.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.itegeko.legal.payloads.internal.ArticlesForIndexingResponse;
import rw.itegeko.legal.payloads.internal.EmbeddingBatchUpsertRequest;
import rw.itegeko.legal.payloads.internal.IndexResponse;
import rw.itegeko.legal.payloads.internal.KeywordSearchRequest;
import rw.itegeko.legal.payloads.internal.LegalRetrievalResponse;
import rw.itegeko.legal.payloads.internal.VectorSearchRequest;
import rw.itegeko.legal.services.InternalAiDataService;

@RestController
@RequestMapping("/internal/ai")
public class InternalAiDataController {
    private final InternalAiDataService service;
    private final String internalApiKey;

    public InternalAiDataController(
        InternalAiDataService service,
        @Value("${app.internal-api-key}") String internalApiKey
    ) {
        this.service = service;
        this.internalApiKey = internalApiKey;
    }

    @GetMapping("/articles-for-indexing")
    public ArticlesForIndexingResponse listArticlesForIndexing(
        @RequestHeader(name = "X-Internal-API-Key", required = false) String apiKey
    ) {
        requireInternalApiKey(apiKey);
        return service.listArticlesForIndexing();
    }

    @PostMapping("/embeddings")
    public IndexResponse upsertEmbeddings(
        @RequestHeader(name = "X-Internal-API-Key", required = false) String apiKey,
        @RequestBody EmbeddingBatchUpsertRequest request
    ) {
        requireInternalApiKey(apiKey);
        return service.upsertEmbeddings(request);
    }

    @PostMapping("/vector-search")
    public LegalRetrievalResponse searchByVector(
        @RequestHeader(name = "X-Internal-API-Key", required = false) String apiKey,
        @RequestBody VectorSearchRequest request
    ) {
        requireInternalApiKey(apiKey);
        return service.searchByVector(request);
    }

    @PostMapping("/keyword-search")
    public LegalRetrievalResponse fallbackKeywordSearch(
        @RequestHeader(name = "X-Internal-API-Key", required = false) String apiKey,
        @RequestBody KeywordSearchRequest request
    ) {
        requireInternalApiKey(apiKey);
        return service.fallbackKeywordSearch(request);
    }

    private void requireInternalApiKey(String apiKey) {
        if (!internalApiKey.equals(apiKey)) {
            throw new AccessDeniedException("Invalid internal API key.");
        }
    }
}
