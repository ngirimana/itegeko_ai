package rw.itegeko.legal.payloads.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record LegalRetrievalResult(
    UUID id,
    @JsonProperty("article_number") String articleNumber,
    @JsonProperty("article_title") String articleTitle,
    @JsonProperty("article_text") String articleText,
    @JsonProperty("article_status") String articleStatus,
    @JsonProperty("document_title") String documentTitle,
    @JsonProperty("document_status") String documentStatus,
    @JsonProperty("source_url") String sourceUrl,
    @JsonProperty("source_name") String sourceName,
    @JsonProperty("relevance_score") BigDecimal relevanceScore
) {}
