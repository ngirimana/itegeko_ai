package rw.itegeko.legal.payloads.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateLegalDocumentRequest(
    @NotBlank @Size(max = 500) String title,
    @Size(max = 100) String lawNumber,
    LocalDate publicationDate,
    @NotBlank @Size(max = 30) String language,
    @NotBlank @Size(max = 50) String status,
    @Size(max = 2000) String sourceUrl,
    UUID categoryId,
    UUID sourceId,
    @NotEmpty @Valid List<CreateLegalArticleRequest> articles
) {}
