package rw.itegeko.legal.payloads;

import java.util.List;

public record CatalogResponse(
    List<LegalCategoryResponse> categories,
    List<LegalSourceResponse> sources
) {}
