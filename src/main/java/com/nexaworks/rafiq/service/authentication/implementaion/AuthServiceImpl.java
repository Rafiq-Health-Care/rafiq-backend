package com.nexaworks.rafiq.service.authentication.implementaion;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.*;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.utils.AuthSessionManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthSessionManager authSessionManager;

    @Override
    public User getAuthenticateUser() {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userId).orElseThrow();
    }

    @Override
    @Transactional
    public LoginResponse login(String email, String password, HttpServletResponse response) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = (User) authentication.getPrincipal();
        return authSessionManager.createLoginSession(response, user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(HttpServletResponse response, HttpServletRequest request) {
        Token token = tokenService.getToken(authSessionManager.getCookie(request, "refreshToken"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenInvalidException("Invalid Refresh Token");
        }
        User user = token.getUser();
        return authSessionManager.createLoginSession(response, user);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authSessionManager.getCookie(request, "refreshToken");
        Token token = tokenService.getToken(refreshToken);
        tokenService.invalidateRefreshToken(token);
        String jwt = authSessionManager.getCookie(request, "jwt");
        if (jwt != null) {
            jwtService.invalidateJwtToken(jwt);
        }
        authSessionManager.invalidateSession(response);
    }

}
