package rw.itegeko.legal.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rw.itegeko.legal.repositories.LegalArticleRepository;
import rw.itegeko.legal.repositories.LegalArticleSearchRow;

class LegalSearchServiceImplTest {
    private final LegalArticleRepository articleRepository = mock(LegalArticleRepository.class);
    private final LegalSearchServiceImpl service = new LegalSearchServiceImpl(articleRepository);

    @Test
    void blankQueryReturnsPaginatedSummaries() {
        var row = row("Article 1", 0.0);
        var page = new PageImpl<>(List.of(row), PageRequest.of(0, 20), 1);
        when(articleRepository.findAllSummaries(any(Pageable.class))).thenReturn(page);

        var response = service.search("", 0, 20);

        assertEquals(1, response.results().size());
        assertEquals(0, response.page());
        assertEquals(20, response.size());
        assertEquals(1, response.totalElements());
        assertEquals("Article 1", response.results().getFirst().articleNumber());
    }

    @Test
    void keywordSearchTrimsQueryAndReturnsPaginationMetadata() {
        var row = row("Article 2", 0.74);
        var page = new PageImpl<>(List.of(row), PageRequest.of(1, 10), 25);
        when(articleRepository.keywordSearch(eq("annual leave"), any(Pageable.class))).thenReturn(page);

        var response = service.search("  annual leave  ", 1, 10);

        assertEquals(1, response.results().size());
        assertEquals(1, response.page());
        assertEquals(10, response.size());
        assertEquals(25, response.totalElements());
        assertEquals(3, response.totalPages());
        assertEquals(0.74, response.results().getFirst().relevanceScore());
        verify(articleRepository).keywordSearch(eq("annual leave"), any(Pageable.class));
    }

    private LegalArticleSearchRow row(String articleNumber, Double relevanceScore) {
        var row = mock(LegalArticleSearchRow.class);
        when(row.getId()).thenReturn(UUID.randomUUID());
        when(row.getArticleNumber()).thenReturn(articleNumber);
        when(row.getArticleTitle()).thenReturn("Annual leave");
        when(row.getArticleText()).thenReturn("Workers are entitled to leave.");
        when(row.getLanguage()).thenReturn("English");
        when(row.getStatus()).thenReturn("verified");
        when(row.getRelevanceScore()).thenReturn(relevanceScore);
        when(row.getDocumentId()).thenReturn(UUID.randomUUID());
        when(row.getDocumentTitle()).thenReturn("Labour law");
        when(row.getDocumentStatus()).thenReturn("verified");
        when(row.getSourceUrl()).thenReturn("https://example.test/labour-law");
        when(row.getPublicationDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(row.getCategoryId()).thenReturn(UUID.randomUUID());
        when(row.getCategoryName()).thenReturn("Labour and Employment");
        when(row.getCategorySlug()).thenReturn("labour-employment");
        when(row.getSourceId()).thenReturn(UUID.randomUUID());
        when(row.getSourceName()).thenReturn("Official Gazette");
        when(row.getSourceType()).thenReturn("gazette");
        when(row.getOfficialUrl()).thenReturn("https://example.test");
        when(row.getSourceVerified()).thenReturn(true);
        return row;
    }
}
