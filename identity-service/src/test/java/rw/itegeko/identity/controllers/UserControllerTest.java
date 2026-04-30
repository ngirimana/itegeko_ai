package rw.itegeko.identity.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import rw.itegeko.identity.payloads.CurrentUserResponse;
import rw.itegeko.identity.payloads.UserActivityResponse;
import rw.itegeko.identity.services.UserActivityService;
import rw.itegeko.identity.services.UserService;

class UserControllerTest {
    @Test
    void delegatesCurrentUserLookupToService() {
        var service = mock(UserService.class);
        var controller = new UserController(service);
        var jwt = jwt();
        var response = new CurrentUserResponse("subject", "user@example.test", null);
        when(service.getCurrentUser(jwt)).thenReturn(response);

        assertEquals(response, controller.me(jwt));
        verify(service).getCurrentUser(jwt);
    }

    @Test
    void delegatesActivityLookupToService() {
        var service = mock(UserActivityService.class);
        var controller = new UserActivityController(service);
        var jwt = jwt();
        var userId = UUID.randomUUID();
        var activities = List.of(new UserActivityResponse(
            UUID.randomUUID(),
            userId,
            "LOGIN",
            "session",
            "1",
            Map.of(),
            OffsetDateTime.parse("2026-01-01T10:00:00Z")
        ));
        when(service.listRecentActivities(userId, jwt)).thenReturn(activities);

        assertEquals(activities, controller.list(userId, jwt));
        verify(service).listRecentActivities(userId, jwt);
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject")
            .claim("email", "user@example.test")
            .build();
    }
}
