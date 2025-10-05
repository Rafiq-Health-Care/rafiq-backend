package com.nexaworks.rafiq.config;


import com.fasterxml.jackson.databind.ObjectMapper;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("OAuth2 user: {}", oAuth2User);
        String fullName = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        // todo generate JWT token if the user is store in database


       if(email == null){
        log.error("Email is null");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), "Cannot find email");
        return;
       }

       Map<String, Object> attributes = Map.of("fullName", fullName, "email", email);
       response.setStatus(HttpServletResponse.SC_OK);
       response.setContentType("application/json");
       new ObjectMapper().writeValue(response.getOutputStream(), attributes);
    }
}
