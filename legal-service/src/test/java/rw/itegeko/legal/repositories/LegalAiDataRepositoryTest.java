package rw.itegeko.legal.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import rw.itegeko.legal.payloads.internal.EmbeddingUpsertRequest;
import rw.itegeko.legal.payloads.internal.LegalRetrievalResult;

class LegalAiDataRepositoryTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final LegalAiDataRepository repository = new LegalAiDataRepository(jdbcTemplate);

    @Test
    void skipsDatabaseWorkWhenNoEmbeddingsAreProvided() {
        var indexed = repository.upsertEmbeddings(List.of());

        assertEquals(0, indexed);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void refreshesVectorTableStatisticsAfterEmbeddingUpsert() {
        var embedding = new EmbeddingUpsertRequest(
            UUID.randomUUID(),
            "test-model",
            List.of(0.1, 0.2),
            "chunk text",
            "hash"
        );

        var indexed = repository.upsertEmbeddings(List.of(embedding));

        assertEquals(1, indexed);
        verify(jdbcTemplate).batchUpdate(anyString(), anyList(), eq(100), any());
        verify(jdbcTemplate).execute("ANALYZE legal.article_embeddings");
    }

    @Test
    void listsArticlesForIndexing() {
        var articleId = UUID.randomUUID();
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<?> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(resultSet(articleId), 0));
        });

        var articles = repository.listArticlesForIndexing();

        assertEquals(articleId, articles.getFirst().id());
        assertEquals("Article text", articles.getFirst().chunkText());
    }

    @Test
    void searchesVectorWithoutCategoryFilter() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(), any(), any())).thenReturn(List.of(result()));

        var results = repository.searchByVector(List.of(0.1, 0.2), "", 3);

        assertEquals(1, results.size());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq("[0.1,0.2]"), eq("[0.1,0.2]"), eq(3));
    }

    @Test
    void searchesVectorWithCategoryFilter() {
        var categoryId = UUID.randomUUID().toString();
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(), any(), any(), any())).thenReturn(List.of(result()));

        var results = repository.searchByVector(List.of(0.1, 0.2), categoryId, 5);

        assertEquals(1, results.size());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq("[0.1,0.2]"), eq(categoryId), eq("[0.1,0.2]"), eq(5));
    }

    @Test
    void performsFallbackKeywordSearch() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(), any(), any(), any())).thenReturn(List.of(result()));

        var results = repository.fallbackKeywordSearch("land", 2);

        assertEquals(1, results.size());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq("%land%"), eq("%land%"), eq("%land%"), eq(2));
    }

    private ResultSet resultSet(UUID articleId) throws SQLException {
        var resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id", UUID.class)).thenReturn(articleId);
        when(resultSet.getString("chunk_text")).thenReturn("Article text");
        return resultSet;
    }

    private LegalRetrievalResult result() {
        return new LegalRetrievalResult(
            UUID.randomUUID(),
            "1",
            "Title",
            "Text",
            "active",
            "Document",
            "active",
            "https://example.test",
            "Gazette",
            BigDecimal.ONE
        );
    }
}
