package rw.itegeko.legal.payloads.internal;

import java.util.UUID;

public record ArticleForIndexingResponse(
    UUID id,
    String chunkText
) {}
