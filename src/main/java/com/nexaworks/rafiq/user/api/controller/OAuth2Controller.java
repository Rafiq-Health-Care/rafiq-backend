package com.nexaworks.rafiq.user.api.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.user.api.dto.request.OAuthRequest;
import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.service.OAuth2Service;

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
@Tag(name = "OAuth2 Authentication")
public class OAuth2Controller {

    @PostMapping("/google")
    @Operation(summary = "Google OAuth2 authentication", description = "Enables social login via Google. Auto-creates account if new user, otherwise authenticates existing user. Eliminates need for separate registration.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    public ResponseEntity<LoginResponse> oAuth2(@RequestBody OAuthRequest request,
            HttpServletResponse response, @Qualifier("googleOAuth2") OAuth2Service google) {
        return ResponseEntity.ok().body(google.oAuth2(request.idToken(), response));
    }
}
