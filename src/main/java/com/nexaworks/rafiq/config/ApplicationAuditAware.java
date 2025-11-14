package com.nexaworks.rafiq.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplicationAuditAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        }

        if (authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            return Optional.of(user.getId());
        }
        return Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}
