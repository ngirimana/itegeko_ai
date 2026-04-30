package rw.itegeko.legal.payloads;

import java.util.List;

public record SearchResponse(
    List<LegalArticleResponse> results,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
