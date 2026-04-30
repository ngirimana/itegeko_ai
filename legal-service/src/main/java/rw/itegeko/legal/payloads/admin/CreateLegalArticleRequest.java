package rw.itegeko.legal.payloads.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLegalArticleRequest(
    @NotBlank @Size(max = 80) String articleNumber,
    @Size(max = 500) String articleTitle,
    @NotBlank @Size(max = 20000) String articleText
) {}
