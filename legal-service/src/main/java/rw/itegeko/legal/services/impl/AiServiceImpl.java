package rw.itegeko.legal.services.impl;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import rw.itegeko.legal.exceptions.ServiceUnavailableException;
import rw.itegeko.legal.payloads.AskQuestionResponse;
import rw.itegeko.legal.services.AiService;

@Service
public class AiServiceImpl implements AiService {
    private final WebClient webClient;
    private final Duration requestTimeout;
    private final long maxRetries;
    private final String internalApiKey;

    public AiServiceImpl(
        @Value("${app.ai-service-url}") String aiServiceUrl,
        @Value("${app.ai.request-timeout-ms:10000}") long requestTimeoutMs,
        @Value("${app.ai.max-retries:2}") long maxRetries,
        @Value("${app.internal-api-key}") String internalApiKey
    ) {
        this(WebClient.builder().baseUrl(aiServiceUrl).build(), requestTimeoutMs, maxRetries, internalApiKey);
    }

    AiServiceImpl(
        WebClient webClient,
        long requestTimeoutMs,
        long maxRetries,
        String internalApiKey
    ) {
        this.webClient = webClient;
        this.requestTimeout = Duration.ofMillis(requestTimeoutMs);
        this.maxRetries = maxRetries;
        this.internalApiKey = internalApiKey;
    }

    @Override
    public AskQuestionResponse ask(Map<String, Object> request) {
        try {
            var response = webClient.post()
                .uri("/v1/legal/ask")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AskQuestionResponse>() {})
                .timeout(requestTimeout)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(200)).filter(this::isRetryable))
                .block(requestTimeout.plusSeconds(5));
            if (response == null) {
                throw new ServiceUnavailableException("AI service returned an empty response.", null);
            }
            return response;
        } catch (RuntimeException exception) {
            if (exception instanceof ServiceUnavailableException serviceUnavailableException) {
                throw serviceUnavailableException;
            }
            throw new ServiceUnavailableException("AI service is unavailable.", exception);
        }
    }

    @Override
    public int indexLegalContent() {
        try {
            var response = webClient.post()
                .uri("/v1/legal/index")
                .header("X-Internal-API-Key", internalApiKey)
                .retrieve()
                .bodyToMono(IndexResponse.class)
                .timeout(requestTimeout)
                .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(200)).filter(this::isRetryable))
                .block(requestTimeout.plusSeconds(5));
            if (response == null) {
                throw new ServiceUnavailableException("AI service returned an empty indexing response.", null);
            }
            return response.indexed();
        } catch (RuntimeException exception) {
            if (exception instanceof ServiceUnavailableException serviceUnavailableException) {
                throw serviceUnavailableException;
            }
            throw new ServiceUnavailableException("AI indexing service is unavailable.", exception);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException) {
            return responseException.getStatusCode().is5xxServerError();
        }
        return true;
    }

    private record IndexResponse(int indexed) {}
}
