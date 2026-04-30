package rw.itegeko.legal.repositories;

import java.util.List;
import java.util.UUID;

public record CreatedLegalDocument(
    UUID documentId,
    List<UUID> articleIds
) {}
