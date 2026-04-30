package rw.itegeko.legal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import rw.itegeko.legal.payloads.AskQuestionRequest;
import rw.itegeko.legal.payloads.AskQuestionResponse;
import rw.itegeko.legal.services.AiService;

@SuppressWarnings("unchecked")
class AiControllerTest {
    private final AiService aiService = Mockito.mock(AiService.class);
    private final AiController controller = new AiController(aiService);

    @Test
    void passesQuestionAndCategoryToAiService() {
        var expected = new AskQuestionResponse(true, Map.of("answerText", "answer"), List.of());
        when(aiService.ask(anyMap())).thenReturn(expected);

        var response = controller.ask(new AskQuestionRequest("What is annual leave?", "labour"));

        assertEquals(expected, response);
        var captor = ArgumentCaptor.forClass(Map.class);
        verify(aiService).ask(captor.capture());
        assertEquals("What is annual leave?", captor.getValue().get("question"));
        assertEquals("labour", captor.getValue().get("categoryId"));
    }

    @Test
    void normalizesMissingCategoryToEmptyString() {
        var expected = new AskQuestionResponse(true, Map.of("answerText", "answer"), List.of());
        when(aiService.ask(anyMap())).thenReturn(expected);

        controller.ask(new AskQuestionRequest("What is notice?", null));

        var captor = ArgumentCaptor.forClass(Map.class);
        verify(aiService).ask(captor.capture());
        assertEquals("", captor.getValue().get("categoryId"));
    }
}
