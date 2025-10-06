package com.nexaworks.rafiq.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.service.JwtService;
import com.nexaworks.rafiq.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtService jwtService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException,
            ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("OAuth2 user: {}", oAuth2User);
      String fullName = oAuth2User.getAttribute("name");
       String email = oAuth2User.getAttribute("email");

        if(email == null){
            log.error("Email is null");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getOutputStream(), "Cannot find email");
            return;
        }

        Optional<User> user = userService.findByEmail(email);
        if(user.isPresent()){
            if (!user.get().isEnabled()){
                // todo handle exception
                throw new IllegalStateException("User is disabled");
            }
            log.info("User already exists");
            String jwt = jwtService.generateToken(user.get());
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getOutputStream(), jwt);
            return;

        }
        if (fullName == null) {
            fullName = "Unknown";
        }
       Map<String, Object> attributes = Map.of("full-name",fullName, "email", email);
       response.setStatus(HttpServletResponse.SC_OK);
       response.setContentType("application/json");
       new ObjectMapper().writeValue(response.getOutputStream(), attributes);
    }
}
