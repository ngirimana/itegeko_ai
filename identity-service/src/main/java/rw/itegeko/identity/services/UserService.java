package rw.itegeko.identity.services;

import org.springframework.security.oauth2.jwt.Jwt;
import rw.itegeko.identity.payloads.CurrentUserResponse;

public interface UserService {
    CurrentUserResponse getCurrentUser(Jwt jwt);
}
