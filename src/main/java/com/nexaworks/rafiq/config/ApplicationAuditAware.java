package com.nexaworks.rafiq.config;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApplicationAuditAware implements AuditorAware<UUID> {

    private final UserRepository userRepository;

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated user cannot access this resource");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return Optional.of(user.getId());
        } else if (principal instanceof DefaultOAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return userRepository.findByEmail(email)
                    .map(User::getId)
                    .or(() -> {
                        throw new IllegalStateException("OAuth2 user not found in database");
                    });
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
    }
}

