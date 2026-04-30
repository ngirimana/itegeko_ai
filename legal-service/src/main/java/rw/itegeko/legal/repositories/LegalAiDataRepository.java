package rw.itegeko.legal.repositories;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import rw.itegeko.legal.payloads.internal.ArticleForIndexingResponse;
import rw.itegeko.legal.payloads.internal.EmbeddingUpsertRequest;
import rw.itegeko.legal.payloads.internal.LegalRetrievalResult;

@Repository
public class LegalAiDataRepository {
    private final JdbcTemplate jdbcTemplate;

    public LegalAiDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ArticleForIndexingResponse> listArticlesForIndexing() {
        var sql = """
            SELECT
              a.id,
              concat_ws(' ', d.title, a.article_number, a.article_title, a.article_text) AS chunk_text
            FROM legal.legal_articles a
            JOIN legal.legal_documents d ON d.id = a.document_id
            ORDER BY d.title, a.order_index NULLS LAST
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ArticleForIndexingResponse(
            rs.getObject("id", UUID.class),
            rs.getString("chunk_text")
        ));
    }

    public int upsertEmbeddings(List<EmbeddingUpsertRequest> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            return 0;
        }
        var sql = """
            INSERT INTO legal.article_embeddings (
              article_id, embedding_model, embedding, chunk_text, chunk_hash
            )
            VALUES (?, ?, ?::vector, ?, ?)
            ON CONFLICT (article_id) DO UPDATE SET
              embedding_model = EXCLUDED.embedding_model,
              embedding = EXCLUDED.embedding,
              chunk_text = EXCLUDED.chunk_text,
              chunk_hash = EXCLUDED.chunk_hash,
              created_at = now()
            """;
        jdbcTemplate.batchUpdate(
            sql,
            embeddings,
            100,
            (statement, embedding) -> {
                statement.setObject(1, embedding.articleId());
                statement.setString(2, embedding.embeddingModel());
                statement.setString(3, toVectorLiteral(embedding.embedding()));
                statement.setString(4, embedding.chunkText());
                statement.setString(5, embedding.chunkHash());
            }
        );
        jdbcTemplate.execute("ANALYZE legal.article_embeddings");
        return embeddings.size();
    }

    public List<LegalRetrievalResult> searchByVector(List<Double> embedding, String categoryId, int limit) {
        var vectorValue = toVectorLiteral(embedding);
        if (categoryId == null || categoryId.isBlank()) {
            var sql = """
                SELECT
                  a.id,
                  a.article_number,
                  a.article_title,
                  a.article_text,
                  a.status AS article_status,
                  d.title AS document_title,
                  d.status AS document_status,
                  d.source_url,
                  s.name AS source_name,
                  1 - (e.embedding <=> ?::vector) AS relevance_score
                FROM legal.article_embeddings e
                JOIN legal.legal_articles a ON a.id = e.article_id
                JOIN legal.legal_documents d ON d.id = a.document_id
                LEFT JOIN legal.legal_sources s ON s.id = d.source_id
                WHERE e.embedding IS NOT NULL
                ORDER BY e.embedding <=> ?::vector
                LIMIT ?
                """;
            return jdbcTemplate.query(sql, this::mapRetrievalResult, vectorValue, vectorValue, limit);
        }
        var sql = """
            SELECT
              a.id,
              a.article_number,
              a.article_title,
              a.article_text,
              a.status AS article_status,
              d.title AS document_title,
              d.status AS document_status,
              d.source_url,
              s.name AS source_name,
              1 - (e.embedding <=> ?::vector) AS relevance_score
            FROM legal.article_embeddings e
            JOIN legal.legal_articles a ON a.id = e.article_id
            JOIN legal.legal_documents d ON d.id = a.document_id
            LEFT JOIN legal.legal_sources s ON s.id = d.source_id
            WHERE e.embedding IS NOT NULL
              AND d.category_id = ?::uuid
            ORDER BY e.embedding <=> ?::vector
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, this::mapRetrievalResult, vectorValue, categoryId, vectorValue, limit);
    }

    public List<LegalRetrievalResult> fallbackKeywordSearch(String query, int limit) {
        var sql = """
            SELECT
              a.id,
              a.article_number,
              a.article_title,
              a.article_text,
              a.status AS article_status,
              d.title AS document_title,
              d.status AS document_status,
              d.source_url,
              s.name AS source_name,
              0.25 AS relevance_score
            FROM legal.legal_articles a
            JOIN legal.legal_documents d ON d.id = a.document_id
            LEFT JOIN legal.legal_sources s ON s.id = d.source_id
            WHERE lower(a.article_text) LIKE lower(?)
               OR lower(a.article_title) LIKE lower(?)
               OR lower(d.title) LIKE lower(?)
            LIMIT ?
            """;
        var pattern = "%" + query + "%";
        return jdbcTemplate.query(sql, this::mapRetrievalResult, pattern, pattern, pattern, limit);
    }

    private LegalRetrievalResult mapRetrievalResult(ResultSet rs, int rowNum) throws SQLException {
        return new LegalRetrievalResult(
            rs.getObject("id", UUID.class),
            rs.getString("article_number"),
            rs.getString("article_title"),
            rs.getString("article_text"),
            rs.getString("article_status"),
            rs.getString("document_title"),
            rs.getString("document_status"),
            rs.getString("source_url"),
            rs.getString("source_name"),
            rs.getBigDecimal("relevance_score")
        );
    }

    private String toVectorLiteral(List<Double> embedding) {
        return "[" + embedding.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(",")) + "]";
    }
}
