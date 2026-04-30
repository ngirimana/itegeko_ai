package rw.itegeko.identity.payloads;

import java.time.OffsetDateTime;
import java.util.UUID;
import rw.itegeko.identity.entities.AppUser;

public record UserProfileResponse(
    UUID id,
    String fullName,
    String email,
    String phone,
    String status,
    OffsetDateTime lastLoginAt,
    OffsetDateTime createdAt
) {
    public static UserProfileResponse from(AppUser user) {
        if (user == null) {
            return null;
        }
        return new UserProfileResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            user.getStatus(),
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }
}
