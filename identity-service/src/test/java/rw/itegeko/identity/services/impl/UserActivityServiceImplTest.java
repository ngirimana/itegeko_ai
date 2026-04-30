package rw.itegeko.identity.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import rw.itegeko.identity.entities.AppUser;
import rw.itegeko.identity.entities.UserActivity;
import rw.itegeko.identity.repositories.AppUserRepository;
import rw.itegeko.identity.repositories.UserActivityRepository;

class UserActivityServiceImplTest {
    private final UserActivityRepository activityRepository = mock(UserActivityRepository.class);
    private final AppUserRepository userRepository = mock(AppUserRepository.class);
    private final UserActivityServiceImpl service = new UserActivityServiceImpl(activityRepository, userRepository);

    @Test
    void ownerCanReadOwnActivities() {
        var userId = UUID.randomUUID();
        var jwt = jwt("owner@example.com", List.of("PUBLIC_USER"));
        var user = mock(AppUser.class);
        var activity = mock(UserActivity.class);

        when(user.getId()).thenReturn(userId);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
        when(activityRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(activity));

        var activities = service.listRecentActivities(userId, jwt);

        assertEquals(1, activities.size());
        verify(activityRepository).findTop50ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void nonOwnerCannotReadActivities() {
        var requestedUserId = UUID.randomUUID();
        var jwt = jwt("other@example.com", List.of("PUBLIC_USER"));
        var user = mock(AppUser.class);

        when(user.getId()).thenReturn(UUID.randomUUID());
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> service.listRecentActivities(requestedUserId, jwt));
        verify(activityRepository, never()).findTop50ByUserIdOrderByCreatedAtDesc(requestedUserId);
    }

    @Test
    void adminCanReadAnyUserActivities() {
        var requestedUserId = UUID.randomUUID();
        var jwt = jwt("admin@example.com", List.of("ADMIN"));

        when(activityRepository.findTop50ByUserIdOrderByCreatedAtDesc(requestedUserId)).thenReturn(List.of());

        var activities = service.listRecentActivities(requestedUserId, jwt);

        assertEquals(0, activities.size());
        verify(userRepository, never()).findByEmail("admin@example.com");
    }

    private Jwt jwt(String email, List<String> roles) {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject")
            .claim("email", email)
            .claim("realm_access", Map.of("roles", roles))
            .build();
    }
}
