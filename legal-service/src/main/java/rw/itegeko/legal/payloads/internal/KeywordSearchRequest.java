package rw.itegeko.legal.payloads.internal;

public record KeywordSearchRequest(
    String query,
    Integer limit
) {}
