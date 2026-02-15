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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
@Tag(name = "OAuth2 Authentication", description = "Social login endpoints for Google and other OAuth2 providers")
public class OAuth2Controller {

    @PostMapping("/google")
    @Operation(summary = "Google OAuth2 login", description = "Authenticates user via Google. Auto-creates account if new user.")
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    public ResponseEntity<LoginResponse> oAuth2(@RequestBody OAuthRequest request,
            HttpServletResponse response, @Qualifier("googleOAuth2") OAuth2Service google)
            throws GeneralSecurityException, IOException {
        return ResponseEntity.ok().body(google.oAuth2(request.idToken(), response));
    }
}
