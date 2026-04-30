package rw.itegeko.legal.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import rw.itegeko.legal.constants.ApiPaths;
import rw.itegeko.legal.payloads.CatalogResponse;
import rw.itegeko.legal.payloads.SearchResponse;
import rw.itegeko.legal.services.LegalCatalogService;
import rw.itegeko.legal.services.LegalSearchService;

@Validated
@RestController
public class LegalController {
    private final LegalCatalogService catalogService;
    private final LegalSearchService searchService;

    public LegalController(
        LegalCatalogService catalogService,
        LegalSearchService searchService
    ) {
        this.catalogService = catalogService;
        this.searchService = searchService;
    }

    @GetMapping(ApiPaths.CATALOG)
    public CatalogResponse catalog() {
        return catalogService.getCatalog();
    }

    @GetMapping(ApiPaths.SEARCH)
    public SearchResponse search(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return searchService.search(q, page, size);
    }
}
