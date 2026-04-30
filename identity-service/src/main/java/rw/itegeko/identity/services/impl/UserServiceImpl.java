package rw.itegeko.identity.services.impl;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import rw.itegeko.identity.payloads.CurrentUserResponse;
import rw.itegeko.identity.payloads.UserProfileResponse;
import rw.itegeko.identity.repositories.AppUserRepository;
import rw.itegeko.identity.services.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final AppUserRepository userRepository;

    public UserServiceImpl(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CurrentUserResponse getCurrentUser(Jwt jwt) {
        var email = jwt.getClaimAsString("email");
        var user = userRepository.findByEmail(email).orElse(null);
        return new CurrentUserResponse(jwt.getSubject(), email == null ? "" : email, UserProfileResponse.from(user));
    }
}
