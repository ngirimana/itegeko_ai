package rw.itegeko.legal.services;

import java.util.Map;
import rw.itegeko.legal.payloads.AskQuestionResponse;

public interface AiService {
    AskQuestionResponse ask(Map<String, Object> request);
    int indexLegalContent();
}
