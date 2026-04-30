package rw.itegeko.identity.services.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import rw.itegeko.identity.entities.UserActivity;
import rw.itegeko.identity.payloads.UserActivityResponse;
import rw.itegeko.identity.repositories.AppUserRepository;
import rw.itegeko.identity.repositories.UserActivityRepository;
import rw.itegeko.identity.services.UserActivityService;

@Service
public class UserActivityServiceImpl implements UserActivityService {
    private final UserActivityRepository repository;
    private final AppUserRepository userRepository;

    public UserActivityServiceImpl(UserActivityRepository repository, AppUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public List<UserActivityResponse> listRecentActivities(UUID userId, Jwt jwt) {
        if (!canReadUserActivities(userId, jwt)) {
            throw new AccessDeniedException("You can only view your own activity history.");
        }
        return repository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private boolean canReadUserActivities(UUID userId, Jwt jwt) {
        if (hasRealmRole(jwt, "ADMIN")) {
            return true;
        }
        var email = jwt.getClaimAsString("email");
        return email != null && userRepository.findByEmail(email)
            .map(user -> user.getId().equals(userId))
            .orElse(false);
    }

    private boolean hasRealmRole(Jwt jwt, String role) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Object roles = realmAccess == null ? null : realmAccess.get("roles");
        return roles instanceof Collection<?> roleNames && roleNames.contains(role);
    }

    private UserActivityResponse toResponse(UserActivity activity) {
        return new UserActivityResponse(
            activity.getId(),
            activity.getUserId(),
            activity.getAction(),
            activity.getResourceType(),
            activity.getResourceId(),
            activity.getMetadata(),
            activity.getCreatedAt()
        );
    }
}
