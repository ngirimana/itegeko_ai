package rw.itegeko.identity.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.itegeko.identity.constants.ApiPaths;
import rw.itegeko.identity.payloads.CurrentUserResponse;
import rw.itegeko.identity.services.UserService;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(ApiPaths.CURRENT_USER)
    public CurrentUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getCurrentUser(jwt);
    }
}
