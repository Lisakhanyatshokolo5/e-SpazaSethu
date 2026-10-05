package za.co.espaza.backend.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the currently authenticated user from the Spring Security context.
 * Controllers use this so that the user id is never taken from the request body.
 */
@Component
public class CurrentUserService {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            if (userPrincipal.getUserId() == null) {
                throw new AuthenticationCredentialsNotFoundException(
                        "Authenticated user has no user id in the security context");
            }
            return userPrincipal.getUserId();
        }

        throw new AuthenticationCredentialsNotFoundException(
                "Authenticated principal does not carry a user id");
    }
}
