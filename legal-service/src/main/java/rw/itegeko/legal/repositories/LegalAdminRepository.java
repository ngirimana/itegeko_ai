package rw.itegeko.legal.repositories;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rw.itegeko.legal.payloads.admin.CreateLegalArticleRequest;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentRequest;

@Repository
public class LegalAdminRepository {
    private final JdbcTemplate jdbcTemplate;

    public LegalAdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public CreatedLegalDocument createDocument(CreateLegalDocumentRequest request) {
        var documentId = UUID.randomUUID();
        var articleIds = request.articles().stream().map(_article -> UUID.randomUUID()).toList();
        insertDocument(documentId, request);
        insertArticles(documentId, articleIds, request);
        return new CreatedLegalDocument(documentId, articleIds);
    }

    private void insertDocument(UUID documentId, CreateLegalDocumentRequest request) {
        var sql = """
            INSERT INTO legal.legal_documents (
              id, category_id, source_id, title, law_number, publication_date, effective_date,
              language, status, source_url, extraction_status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'completed')
            """;
        jdbcTemplate.update(sql, statement -> {
            statement.setObject(1, documentId);
            setNullableUuid(statement, 2, request.categoryId());
            setNullableUuid(statement, 3, request.sourceId());
            statement.setString(4, request.title().trim());
            statement.setString(5, trimToNull(request.lawNumber()));
            setNullableDate(statement, 6, request.publicationDate());
            setNullableDate(statement, 7, request.publicationDate());
            statement.setString(8, request.language().trim());
            statement.setString(9, request.status().trim());
            statement.setString(10, trimToNull(request.sourceUrl()));
        });
    }

    private void insertArticles(UUID documentId, List<UUID> articleIds, CreateLegalDocumentRequest request) {
        var sql = """
            INSERT INTO legal.legal_articles (
              id, document_id, article_number, article_title, article_text, language, status, order_index
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        var rows = new java.util.ArrayList<ArticleInsertRow>();
        for (int index = 0; index < request.articles().size(); index++) {
            rows.add(new ArticleInsertRow(articleIds.get(index), request.articles().get(index), index + 1));
        }
        jdbcTemplate.batchUpdate(sql, rows, 100, (statement, row) -> {
            statement.setObject(1, row.id());
            statement.setObject(2, documentId);
            statement.setString(3, row.article().articleNumber().trim());
            statement.setString(4, trimToNull(row.article().articleTitle()));
            statement.setString(5, row.article().articleText().trim());
            statement.setString(6, request.language().trim());
            statement.setString(7, request.status().trim());
            statement.setInt(8, row.orderIndex());
        });
    }

    private void setNullableUuid(java.sql.PreparedStatement statement, int index, UUID value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.OTHER);
            return;
        }
        statement.setObject(index, value);
    }

    private void setNullableDate(java.sql.PreparedStatement statement, int index, LocalDate value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
            return;
        }
        statement.setDate(index, Date.valueOf(value));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record ArticleInsertRow(UUID id, CreateLegalArticleRequest article, int orderIndex) {}
}
