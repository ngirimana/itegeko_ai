package rw.itegeko.legal.payloads.internal;

import java.util.List;

public record LegalRetrievalResponse(
    List<LegalRetrievalResult> results
) {}
