package rw.itegeko.legal.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import rw.itegeko.legal.entities.LegalCategory;
import rw.itegeko.legal.entities.LegalSource;
import rw.itegeko.legal.repositories.LegalCategoryRepository;
import rw.itegeko.legal.repositories.LegalSourceRepository;

class LegalCatalogServiceImplTest {
    private final LegalCategoryRepository categoryRepository = mock(LegalCategoryRepository.class);
    private final LegalSourceRepository sourceRepository = mock(LegalSourceRepository.class);
    private final LegalCatalogServiceImpl service = new LegalCatalogServiceImpl(categoryRepository, sourceRepository);

    @Test
    void mapsCategoriesAndSourcesToApiDtos() {
        var categoryId = UUID.randomUUID();
        var sourceId = UUID.randomUUID();
        var category = mock(LegalCategory.class);
        var source = mock(LegalSource.class);

        when(category.getId()).thenReturn(categoryId);
        when(category.getName()).thenReturn("Labour and Employment");
        when(category.getSlug()).thenReturn("labour-employment");
        when(category.getStatus()).thenReturn("active");
        when(source.getId()).thenReturn(sourceId);
        when(source.getName()).thenReturn("Official Gazette");
        when(source.getSourceType()).thenReturn("gazette");
        when(source.getOfficialUrl()).thenReturn("https://example.test");
        when(source.getTrustLevel()).thenReturn("official");
        when(source.isVerified()).thenReturn(true);
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(sourceRepository.findAll()).thenReturn(List.of(source));

        var response = service.getCatalog();

        assertEquals(categoryId, response.categories().getFirst().id());
        assertEquals("labour-employment", response.categories().getFirst().slug());
        assertEquals(sourceId, response.sources().getFirst().id());
        assertEquals("official", response.sources().getFirst().trustLevel());
        assertEquals(true, response.sources().getFirst().verified());
    }
}
