package rw.itegeko.legal.payloads.internal;

import java.util.List;

public record EmbeddingBatchUpsertRequest(
    List<EmbeddingUpsertRequest> embeddings
) {}
