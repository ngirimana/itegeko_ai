package rw.itegeko.legal.payloads.internal;

import java.util.List;
import java.util.UUID;

public record EmbeddingUpsertRequest(
    UUID articleId,
    String embeddingModel,
    List<Double> embedding,
    String chunkText,
    String chunkHash
) {}
