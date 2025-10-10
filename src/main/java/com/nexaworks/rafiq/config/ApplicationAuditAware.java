package com.nexaworks.rafiq.config;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
        if(authentication==null){
            return Optional.of(
                    UUID.fromString("00000000-0000-0000-0000-000000000000"));
        }
        if(authentication.getPrincipal() instanceof OAuth2User){
            String email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
            return userRepository.findByEmail(email).map(User::getId);
        }
        if( authentication.isAuthenticated()&&!authentication.getName().equals("anonymousUser")){
            User user = (User) authentication.getPrincipal();
            return Optional.of(user.getId());
        }
        return Optional.of(
                UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}

