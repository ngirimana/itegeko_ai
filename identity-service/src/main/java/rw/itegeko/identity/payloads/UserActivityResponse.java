package rw.itegeko.identity.payloads;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record UserActivityResponse(
    UUID id,
    UUID userId,
    String action,
    String resourceType,
    String resourceId,
    Map<String, Object> metadata,
    OffsetDateTime createdAt
) {}
