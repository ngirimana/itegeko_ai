package rw.itegeko.legal.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import rw.itegeko.legal.payloads.admin.CreateLegalArticleRequest;
import rw.itegeko.legal.payloads.admin.CreateLegalDocumentRequest;

class LegalAdminRepositoryTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final LegalAdminRepository repository = new LegalAdminRepository(jdbcTemplate);

    @Test
    void createsDocumentWithTrimmedOptionalValues() throws Exception {
        var categoryId = UUID.randomUUID();
        var sourceId = UUID.randomUUID();
        var request = request(categoryId, sourceId, " LAW-1 ", " https://example.test/law ");
        captureDocumentInsert();
        captureArticleInsert();

        var created = repository.createDocument(request);

        assertNotNull(created.documentId());
        assertEquals(1, created.articleIds().size());
    }

    @Test
    void createsDocumentWithNullOptionalValues() throws Exception {
        var request = request(null, null, " ", " ");
        captureDocumentInsert();
        captureArticleInsert();

        var created = repository.createDocument(request);

        assertNotNull(created.documentId());
        assertEquals(1, created.articleIds().size());
    }

    private void captureDocumentInsert() throws Exception {
        when(jdbcTemplate.update(anyString(), any(PreparedStatementSetter.class))).thenAnswer(invocation -> {
            var statement = mock(PreparedStatement.class);
            PreparedStatementSetter setter = invocation.getArgument(1);
            setter.setValues(statement);
            verify(statement).setString(4, "New Law");
            return 1;
        });
    }

    private void captureArticleInsert() throws Exception {
        when(jdbcTemplate.batchUpdate(anyString(), any(List.class), any(Integer.class), any())).thenAnswer(invocation -> {
            var statement = mock(PreparedStatement.class);
            @SuppressWarnings("unchecked")
            List<Object> rows = invocation.getArgument(1);
            @SuppressWarnings("unchecked")
            ParameterizedPreparedStatementSetter<Object> setter = invocation.getArgument(3);
            setter.setValues(statement, rows.getFirst());
            verify(statement).setString(3, "Article 1");
            verify(statement).setString(4, "Scope");
            verify(statement).setString(5, "This law applies.");
            verify(statement).setInt(8, 1);
            return new int[][] { { 1 } };
        });
    }

    private CreateLegalDocumentRequest request(UUID categoryId, UUID sourceId, String lawNumber, String sourceUrl) {
        return new CreateLegalDocumentRequest(
            " New Law ",
            lawNumber,
            LocalDate.parse("2026-01-01"),
            " en ",
            " active ",
            sourceUrl,
            categoryId,
            sourceId,
            List.of(new CreateLegalArticleRequest(" Article 1 ", " Scope ", " This law applies. "))
        );
    }
}
