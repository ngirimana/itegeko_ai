package rw.itegeko.legal.payloads.admin;

import java.util.List;
import java.util.UUID;

public record CreateLegalDocumentResponse(
    UUID documentId,
    List<UUID> articleIds,
    int indexedArticles,
    String indexingStatus
) {}
