package com.nexaworks.rafiq.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.dto.request.auth.OAuthRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.service.authentication.OAuth2Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> oAuth2(@RequestBody OAuthRequest request,
            HttpServletResponse response, @Qualifier("googleOAuth2") OAuth2Service google)
            throws GeneralSecurityException, IOException {
        return ResponseEntity.ok().body(google.oAuth2(request.idToken(), response));
    }
}
