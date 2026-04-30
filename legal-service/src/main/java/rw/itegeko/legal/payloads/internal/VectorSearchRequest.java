package rw.itegeko.legal.payloads.internal;

import java.util.List;

public record VectorSearchRequest(
    List<Double> embedding,
    String categoryId,
    Integer limit
) {}
