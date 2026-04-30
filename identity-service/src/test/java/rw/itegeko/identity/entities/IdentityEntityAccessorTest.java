package rw.itegeko.identity.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdentityEntityAccessorTest {
    @Test
    void appUserAccessorsExposeMappedFields() throws Exception {
        var user = new AppUser();
        var id = UUID.randomUUID();
        var lastLogin = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        var createdAt = OffsetDateTime.parse("2025-01-01T10:00:00Z");
        set(user, "id", id);
        set(user, "fullName", "System Admin");
        set(user, "email", "admin@example.test");
        set(user, "phone", "+250700000000");
        set(user, "externalAuthId", "keycloak-id");
        set(user, "status", "active");
        set(user, "lastLoginAt", lastLogin);
        set(user, "createdAt", createdAt);

        assertEquals(id, user.getId());
        assertEquals("System Admin", user.getFullName());
        assertEquals("admin@example.test", user.getEmail());
        assertEquals("+250700000000", user.getPhone());
        assertEquals("keycloak-id", user.getExternalAuthId());
        assertEquals("active", user.getStatus());
        assertEquals(lastLogin, user.getLastLoginAt());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void userActivityAccessorsExposeMappedFields() throws Exception {
        var activity = new UserActivity();
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var createdAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        var metadata = Map.<String, Object>of("ip", "127.0.0.1");
        set(activity, "id", id);
        set(activity, "userId", userId);
        set(activity, "action", "LOGIN");
        set(activity, "resourceType", "session");
        set(activity, "resourceId", "session-1");
        set(activity, "metadata", metadata);
        set(activity, "createdAt", createdAt);

        assertEquals(id, activity.getId());
        assertEquals(userId, activity.getUserId());
        assertEquals("LOGIN", activity.getAction());
        assertEquals("session", activity.getResourceType());
        assertEquals("session-1", activity.getResourceId());
        assertEquals(metadata, activity.getMetadata());
        assertEquals(createdAt, activity.getCreatedAt());
    }

    private void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
