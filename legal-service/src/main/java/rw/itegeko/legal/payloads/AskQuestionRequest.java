package rw.itegeko.legal.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskQuestionRequest(
    @NotBlank @Size(max = 1000) String question,
    String categoryId
) {}
