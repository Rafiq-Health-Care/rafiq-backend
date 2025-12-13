package com.nexaworks.rafiq.user.utils;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.exception.TokenInvalidException;
import com.nexaworks.rafiq.user.service.JwtService;
import com.nexaworks.rafiq.user.service.TokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthSessionManager {
    private final JwtService jwtService;
    private final TokenService tokenService;

    @NotNull
    public LoginResponse createLoginSession(HttpServletResponse response, User user) {
        String jwt = jwtService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        addTokenToCookie(response, jwt, "jwt", 60 * 60 * 24 * 7);
        addTokenToCookie(response, refreshToken, "refreshToken", 60 * 60 * 24 * 30);
        return new LoginResponse(user.getRoles().stream().map(Role::getName)
                .filter(role -> !role.equals("ROLE_USER")).findFirst());
    }

    public void addTokenToCookie(HttpServletResponse response, String token, String cookieName,
            int expiry) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, token).httpOnly(true).secure(false)
                .sameSite("Lax").path("/").maxAge(expiry).build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public String getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        throw new TokenInvalidException("Invalid or missing token");
    }

    public void invalidateSession(HttpServletResponse response) {
        addTokenToCookie(response, null, "jwt", 0);
        addTokenToCookie(response, null, "refreshToken", 0);
    }
}
