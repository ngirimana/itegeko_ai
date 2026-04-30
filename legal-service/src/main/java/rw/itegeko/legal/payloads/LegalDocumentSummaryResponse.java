package rw.itegeko.legal.payloads;

import java.time.LocalDate;
import java.util.UUID;

public record LegalDocumentSummaryResponse(
    UUID id,
    String title,
    String status,
    String sourceUrl,
    LocalDate publicationDate,
    LegalCategoryResponse category,
    LegalSourceResponse source
) {}
