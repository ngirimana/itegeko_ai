package rw.itegeko.legal.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
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

    @Test
    void mapsMissingRealmRolesToNoAuthorities() {
        var converter = new SecurityConfig().jwtAuthenticationConverter();
        var authentication = converter.convert(Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject")
            .build());

        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    void buildsCorsConfigurationFromAllowedOrigins() throws Exception {
        var config = new SecurityConfig();
        set(config, "allowedOrigins", "http://localhost:3000,https://itegeko.test");

        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(new MockHttpServletRequest());

        assertEquals(List.of("http://localhost:3000", "https://itegeko.test"), cors.getAllowedOrigins());
        assertTrue(cors.getAllowedMethods().contains("POST"));
        assertEquals(List.of("*"), cors.getAllowedHeaders());
        assertTrue(cors.getAllowCredentials());
    }

    private Jwt jwt(List<String> roles) {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject")
            .claim("realm_access", Map.of("roles", roles))
            .build();
    }

    private void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
