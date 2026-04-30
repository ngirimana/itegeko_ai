package rw.itegeko.legal.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rw.itegeko.legal.exceptions.ServiceUnavailableException;

class AiServiceImplTest {
    @Test
    void asksAiServiceAndReturnsResponse() {
        var service = service(HttpStatus.OK, """
            {"supported":true,"answer":{"answerText":"Yes","confidenceLevel":"high"},"sources":[]}
            """);

        var response = service.ask(Map.of("question", "Can I register a company?"));

        assertEquals(true, response.supported());
        assertEquals("Yes", response.answer().get("answerText"));
    }

    @Test
    void indexesLegalContentWithInternalApiKey() {
        var service = service(HttpStatus.OK, "{\"indexed\":7}");

        assertEquals(7, service.indexLegalContent());
    }

    @Test
    void wrapsAiServiceFailures() {
        var service = service(HttpStatus.INTERNAL_SERVER_ERROR, "{\"message\":\"down\"}");

        assertThrows(ServiceUnavailableException.class, () -> service.ask(Map.of("question", "Q?")));
    }

    private AiServiceImpl service(HttpStatus status, String body) {
        var webClient = WebClient.builder()
            .exchangeFunction(request -> Mono.just(ClientResponse.create(status)
                .header("Content-Type", "application/json")
                .body(body)
                .build()))
            .build();
        return new AiServiceImpl(webClient, 1000, 0, "secret");
    }
}
