package rw.itegeko.legal.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import rw.itegeko.legal.payloads.internal.EmbeddingUpsertRequest;

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
}
