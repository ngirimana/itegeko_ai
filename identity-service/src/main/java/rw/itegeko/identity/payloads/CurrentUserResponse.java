package rw.itegeko.identity.payloads;

public record CurrentUserResponse(
    String subject,
    String email,
    UserProfileResponse profile
) {}
