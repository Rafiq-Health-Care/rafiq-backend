package com.nexaworks.rafiq.config;

import com.nexaworks.rafiq.entities.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ApplicationAuditAware implements AuditorAware<UUID> {
    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null|| !authentication.isAuthenticated()) {
            // todo handle exception
            throw new IllegalStateException("Unauthenticated user cannot access this resource");
        }
        User user = (User) authentication.getPrincipal();
        return Optional.of(user.getId());
    }
}
