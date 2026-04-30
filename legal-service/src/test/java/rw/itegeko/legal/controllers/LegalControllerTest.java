package rw.itegeko.legal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import rw.itegeko.legal.payloads.AskQuestionRequest;
import rw.itegeko.legal.payloads.AskQuestionResponse;
import rw.itegeko.legal.payloads.CatalogResponse;
import rw.itegeko.legal.payloads.SearchResponse;
import rw.itegeko.legal.services.AiService;
import rw.itegeko.legal.services.LegalCatalogService;
import rw.itegeko.legal.services.LegalSearchService;

class LegalControllerTest {
    @Test
    void delegatesCatalogAndSearchRequests() {
        var catalogService = mock(LegalCatalogService.class);
        var searchService = mock(LegalSearchService.class);
        var controller = new LegalController(catalogService, searchService);
        var catalog = new CatalogResponse(List.of(), List.of());
        var search = new SearchResponse(List.of(), 1, 10, 0, 0);
        when(catalogService.getCatalog()).thenReturn(catalog);
        when(searchService.search("land", 1, 10)).thenReturn(search);

        assertEquals(catalog, controller.catalog());
        assertEquals(search, controller.search("land", 1, 10));
        verify(catalogService).getCatalog();
        verify(searchService).search("land", 1, 10);
    }

    @Test
    void askControllerMapsNullCategoryToEmptyString() {
        var aiService = mock(AiService.class);
        var controller = new AiController(aiService);
        var response = new AskQuestionResponse(true, Map.of("answer", "Yes"), List.of());
        when(aiService.ask(argThat(request ->
            request.get("question").equals("Can I register a company?")
                && request.get("categoryId").equals("")
        ))).thenReturn(response);

        assertEquals(response, controller.ask(new AskQuestionRequest("Can I register a company?", null)));
    }
}
