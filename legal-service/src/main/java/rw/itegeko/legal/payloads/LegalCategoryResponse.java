package rw.itegeko.legal.payloads;

import java.util.UUID;
import rw.itegeko.legal.entities.LegalCategory;

public record LegalCategoryResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String status
) {
    public static LegalCategoryResponse from(LegalCategory category) {
        return new LegalCategoryResponse(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            category.getStatus()
        );
    }
}
