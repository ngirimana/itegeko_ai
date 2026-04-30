package rw.itegeko.identity.services;

import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import rw.itegeko.identity.payloads.UserActivityResponse;

public interface UserActivityService {
    List<UserActivityResponse> listRecentActivities(UUID userId, Jwt jwt);
}
