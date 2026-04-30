package rw.itegeko.legal.payloads;

import java.util.UUID;
import rw.itegeko.legal.repositories.LegalArticleSearchRow;

public record LegalArticleResponse(
    UUID id,
    String articleNumber,
    String articleTitle,
    String articleText,
    String language,
    String status,
    Double relevanceScore,
    LegalDocumentSummaryResponse document
) {
    public static LegalArticleResponse from(LegalArticleSearchRow row) {
        var category = row.getCategoryId() == null
            ? null
            : new LegalCategoryResponse(
                row.getCategoryId(),
                row.getCategoryName(),
                row.getCategorySlug(),
                null,
                null
            );
        var source = row.getSourceId() == null
            ? null
            : new LegalSourceResponse(
                row.getSourceId(),
                row.getSourceName(),
                row.getSourceType(),
                row.getOfficialUrl(),
                null,
                null,
                Boolean.TRUE.equals(row.getSourceVerified())
            );
        var document = new LegalDocumentSummaryResponse(
            row.getDocumentId(),
            row.getDocumentTitle(),
            row.getDocumentStatus(),
            row.getSourceUrl(),
            row.getPublicationDate(),
            category,
            source
        );
        return new LegalArticleResponse(
            row.getId(),
            row.getArticleNumber(),
            row.getArticleTitle(),
            row.getArticleText(),
            row.getLanguage(),
            row.getStatus(),
            row.getRelevanceScore(),
            document
        );
    }
}
