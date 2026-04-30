package rw.itegeko.identity.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class SecurityConfigTest {
    @Test
    void mapsKeycloakRealmRolesToSpringAuthorities() {
        var converter = new SecurityConfig().jwtAuthenticationConverter();
        var authentication = converter.convert(jwt(List.of("ADMIN", "LEGAL_REVIEWER")));

        assertTrue(authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_LEGAL_REVIEWER")));
    }

    private Jwt jwt(List<String> roles) {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject")
            .claim("realm_access", Map.of("roles", roles))
            .build();
    }
}
