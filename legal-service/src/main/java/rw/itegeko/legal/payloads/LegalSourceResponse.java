package rw.itegeko.legal.payloads;

import java.util.UUID;
import rw.itegeko.legal.entities.LegalSource;

public record LegalSourceResponse(
    UUID id,
    String name,
    String sourceType,
    String officialUrl,
    String institution,
    String trustLevel,
    boolean verified
) {
    public static LegalSourceResponse from(LegalSource source) {
        return new LegalSourceResponse(
            source.getId(),
            source.getName(),
            source.getSourceType(),
            source.getOfficialUrl(),
            source.getInstitution(),
            source.getTrustLevel(),
            source.isVerified()
        );
    }
}
