package rw.itegeko.identity.controllers;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rw.itegeko.identity.constants.ApiPaths;
import rw.itegeko.identity.payloads.UserActivityResponse;
import rw.itegeko.identity.services.UserActivityService;

@RestController
public class UserActivityController {
    private final UserActivityService activityService;

    public UserActivityController(UserActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping(ApiPaths.USER_ACTIVITIES)
    public List<UserActivityResponse> list(@RequestParam UUID userId, @AuthenticationPrincipal Jwt jwt) {
        return activityService.listRecentActivities(userId, jwt);
    }
}
