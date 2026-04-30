package rw.itegeko.legal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import rw.itegeko.legal.payloads.internal.ArticleForIndexingResponse;
import rw.itegeko.legal.payloads.internal.ArticlesForIndexingResponse;
import rw.itegeko.legal.payloads.internal.KeywordSearchRequest;
import rw.itegeko.legal.payloads.internal.VectorSearchRequest;
import rw.itegeko.legal.services.InternalAiDataService;

class InternalAiDataControllerTest {
    private final InternalAiDataService service = mock(InternalAiDataService.class);
    private final InternalAiDataController controller = new InternalAiDataController(service, "test-key");

    @Test
    void rejectsMissingInternalApiKey() {
        assertThrows(AccessDeniedException.class, () -> controller.listArticlesForIndexing(null));
    }

    @Test
    void delegatesArticleIndexingRequestWhenInternalApiKeyIsValid() {
        var expected = new ArticlesForIndexingResponse(List.of(
            new ArticleForIndexingResponse(java.util.UUID.randomUUID(), "chunk")
        ));
        when(service.listArticlesForIndexing()).thenReturn(expected);

        var response = controller.listArticlesForIndexing("test-key");

        assertEquals(expected, response);
        verify(service).listArticlesForIndexing();
    }

    @Test
    void delegatesVectorSearchWhenInternalApiKeyIsValid() {
        var request = new VectorSearchRequest(List.of(0.1, 0.2), null, 5);

        controller.searchByVector("test-key", request);

        verify(service).searchByVector(request);
    }

    @Test
    void delegatesKeywordSearchWhenInternalApiKeyIsValid() {
        var request = new KeywordSearchRequest("annual leave", 5);

        controller.fallbackKeywordSearch("test-key", request);

        verify(service).fallbackKeywordSearch(request);
    }
}
