package rw.itegeko.legal.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LegalEntityAccessorTest {
    @Test
    void legalDocumentAccessorsExposeMappedFields() throws Exception {
        var document = new LegalDocument();
        var id = UUID.randomUUID();
        var category = new LegalCategory();
        var source = new LegalSource();
        set(document, "id", id);
        set(document, "category", category);
        set(document, "source", source);
        set(document, "title", "Law title");
        set(document, "lawNumber", "LAW-1");
        set(document, "publicationDate", LocalDate.parse("2026-01-01"));
        set(document, "language", "en");
        set(document, "status", "active");
        set(document, "sourceUrl", "https://example.test");

        assertEquals(id, document.getId());
        assertSame(category, document.getCategory());
        assertSame(source, document.getSource());
        assertEquals("Law title", document.getTitle());
        assertEquals("LAW-1", document.getLawNumber());
        assertEquals(LocalDate.parse("2026-01-01"), document.getPublicationDate());
        assertEquals("en", document.getLanguage());
        assertEquals("active", document.getStatus());
        assertEquals("https://example.test", document.getSourceUrl());
    }

    @Test
    void legalArticleAccessorsExposeMappedFields() throws Exception {
        var article = new LegalArticle();
        var id = UUID.randomUUID();
        var document = new LegalDocument();
        set(article, "id", id);
        set(article, "document", document);
        set(article, "articleNumber", "1");
        set(article, "articleTitle", "Scope");
        set(article, "articleText", "Text");
        set(article, "language", "en");
        set(article, "status", "active");

        assertEquals(id, article.getId());
        assertSame(document, article.getDocument());
        assertEquals("1", article.getArticleNumber());
        assertEquals("Scope", article.getArticleTitle());
        assertEquals("Text", article.getArticleText());
        assertEquals("en", article.getLanguage());
        assertEquals("active", article.getStatus());
    }

    @Test
    void categoryAndSourceAccessorsExposeMappedFields() throws Exception {
        var category = new LegalCategory();
        var categoryId = UUID.randomUUID();
        set(category, "id", categoryId);
        set(category, "name", "Companies");
        set(category, "slug", "companies");
        set(category, "description", "Company law");
        set(category, "status", "active");

        assertEquals(categoryId, category.getId());
        assertEquals("Companies", category.getName());
        assertEquals("companies", category.getSlug());
        assertEquals("Company law", category.getDescription());
        assertEquals("active", category.getStatus());

        var source = new LegalSource();
        var sourceId = UUID.randomUUID();
        set(source, "id", sourceId);
        set(source, "name", "Official Gazette");
        set(source, "sourceType", "gazette");
        set(source, "officialUrl", "https://example.test");
        set(source, "institution", "RLRC");
        set(source, "trustLevel", "official");
        set(source, "verified", false);

        assertEquals(sourceId, source.getId());
        assertEquals("Official Gazette", source.getName());
        assertEquals("gazette", source.getSourceType());
        assertEquals("https://example.test", source.getOfficialUrl());
        assertEquals("RLRC", source.getInstitution());
        assertEquals("official", source.getTrustLevel());
        assertFalse(source.isVerified());
    }

    private void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
