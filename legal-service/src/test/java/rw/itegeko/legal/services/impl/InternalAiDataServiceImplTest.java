package rw.itegeko.legal.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import rw.itegeko.legal.payloads.internal.ArticleForIndexingResponse;
import rw.itegeko.legal.payloads.internal.EmbeddingBatchUpsertRequest;
import rw.itegeko.legal.payloads.internal.KeywordSearchRequest;
import rw.itegeko.legal.payloads.internal.VectorSearchRequest;
import rw.itegeko.legal.repositories.LegalAiDataRepository;

class InternalAiDataServiceImplTest {
    private final LegalAiDataRepository repository = mock(LegalAiDataRepository.class);
    private final InternalAiDataServiceImpl service = new InternalAiDataServiceImpl(repository);

    @Test
    void returnsArticlesForIndexingFromLegalRepository() {
        var articles = List.of(new ArticleForIndexingResponse(java.util.UUID.randomUUID(), "chunk text"));
        when(repository.listArticlesForIndexing()).thenReturn(articles);

        var response = service.listArticlesForIndexing();

        assertEquals(articles, response.articles());
    }

    @Test
    void upsertsEmbeddingsThroughLegalRepository() {
        var request = new EmbeddingBatchUpsertRequest(List.of());
        when(repository.upsertEmbeddings(request.embeddings())).thenReturn(0);

        var response = service.upsertEmbeddings(request);

        assertEquals(0, response.indexed());
        verify(repository).upsertEmbeddings(request.embeddings());
    }

    @Test
    void capsVectorSearchLimit() {
        var request = new VectorSearchRequest(List.of(0.1, 0.2), null, 999);

        service.searchByVector(request);

        verify(repository).searchByVector(request.embedding(), null, 20);
    }

    @Test
    void defaultsKeywordSearchLimit() {
        var request = new KeywordSearchRequest("leave", 0);

        service.fallbackKeywordSearch(request);

        verify(repository).fallbackKeywordSearch("leave", 5);
    }
}
