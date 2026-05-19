package com.nexaworks.rafiq.service.authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.auth.GoogleAuthException;
import com.nexaworks.rafiq.service.user.UserService;
import com.nexaworks.rafiq.utils.AuthSessionManager;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("googleOAuth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2GoogleService implements OAuth2Service {
    private final UserService userService;
    private final AuthSessionManager authSessionManager;
    private final GoogleIdTokenVerifier verifier;
    @Override
    @Transactional
    public LoginResponse oAuth2(String idToken, HttpServletResponse response) {
        GoogleIdToken googleIdToken = getGoogleIdToken(idToken);
        String email = googleIdToken.getPayload().getEmail();
        String firstName = googleIdToken.getPayload().get("given_name").toString();
        String lastName = googleIdToken.getPayload().get("family_name").toString();
        Optional<User> user = userService.getUser(email, firstName, lastName);
        if (user.isPresent()) {
            return authSessionManager.createLoginSession(response, user.get());
        } else {
            throw new GoogleAuthException("Failed to authenticate user with Google");
        }
    }

    private GoogleIdToken getGoogleIdToken(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new GoogleAuthException("Invalid id token");
            }
            return token;
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error verifying Google ID token", e);
            throw new GoogleAuthException("Failed to verify Google ID token");
        }
    }
}
