package rw.itegeko.legal.controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import rw.itegeko.legal.constants.ApiPaths;
import rw.itegeko.legal.payloads.AskQuestionRequest;
import rw.itegeko.legal.payloads.AskQuestionResponse;
import rw.itegeko.legal.services.AiService;
import java.util.Map;

@RestController
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(ApiPaths.ASK)
    public AskQuestionResponse ask(@Valid @RequestBody AskQuestionRequest request) {
        return aiService.ask(Map.of(
            "question", request.question(),
            "categoryId", request.categoryId() == null ? "" : request.categoryId()
        ));
    }
}
