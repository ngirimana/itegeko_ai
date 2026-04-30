package rw.itegeko.legal.repositories;

import java.time.LocalDate;
import java.util.UUID;

public interface LegalArticleSearchRow {
    UUID getId();
    String getArticleNumber();
    String getArticleTitle();
    String getArticleText();
    String getLanguage();
    String getStatus();
    Double getRelevanceScore();
    UUID getDocumentId();
    String getDocumentTitle();
    String getDocumentStatus();
    String getSourceUrl();
    LocalDate getPublicationDate();
    UUID getCategoryId();
    String getCategoryName();
    String getCategorySlug();
    UUID getSourceId();
    String getSourceName();
    String getSourceType();
    String getOfficialUrl();
    Boolean getSourceVerified();
}
