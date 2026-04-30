package rw.itegeko.legal.payloads;

import java.util.List;
import java.util.Map;

public record AskQuestionResponse(
    boolean supported,
    Map<String, Object> answer,
    List<Map<String, Object>> sources
) {}
