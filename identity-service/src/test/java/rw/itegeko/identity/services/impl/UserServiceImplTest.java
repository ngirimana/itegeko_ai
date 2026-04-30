package rw.itegeko.identity.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import rw.itegeko.identity.entities.AppUser;
import rw.itegeko.identity.repositories.AppUserRepository;

class UserServiceImplTest {
    private final AppUserRepository userRepository = mock(AppUserRepository.class);
    private final UserServiceImpl service = new UserServiceImpl(userRepository);

    @Test
    void returnsJwtIdentityAndMappedProfileWhenUserExists() {
        var userId = UUID.randomUUID();
        var user = mock(AppUser.class);
        when(user.getId()).thenReturn(userId);
        when(user.getFullName()).thenReturn("System Admin");
        when(user.getEmail()).thenReturn("admin@itegeko.local");
        when(user.getStatus()).thenReturn("active");
        when(userRepository.findByEmail("admin@itegeko.local")).thenReturn(Optional.of(user));

        var response = service.getCurrentUser(jwt("subject-1", "admin@itegeko.local"));

        assertEquals("subject-1", response.subject());
        assertEquals("admin@itegeko.local", response.email());
        assertEquals(userId, response.profile().id());
        assertEquals("System Admin", response.profile().fullName());
        assertEquals("active", response.profile().status());
    }

    @Test
    void returnsNullProfileWhenJwtEmailIsUnknown() {
        when(userRepository.findByEmail("missing@example.test")).thenReturn(Optional.empty());

        var response = service.getCurrentUser(jwt("subject-2", "missing@example.test"));

        assertEquals("subject-2", response.subject());
        assertEquals("missing@example.test", response.email());
        assertNull(response.profile());
    }

    private Jwt jwt(String subject, String email) {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject(subject)
            .claim("email", email)
            .claim("realm_access", Map.of("roles", java.util.List.of("ADMIN")))
            .build();
    }
}
