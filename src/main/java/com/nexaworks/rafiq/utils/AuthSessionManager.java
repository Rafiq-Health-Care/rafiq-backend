package com.nexaworks.rafiq.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.service.JwtService;
import com.nexaworks.rafiq.service.TokenService;

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
    @Value("${jwt.expiration}")
    private long jwtExpiry;
    @Value("${refresh.expiration}")
    private long refreshTokenExpiry;

    @NotNull
    public LoginResponse createLoginSession(HttpServletResponse response, User user) {
        String jwt = jwtService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        addTokenToCookie(response, jwt, "jwt", (int) jwtExpiry);
        addTokenToCookie(response, refreshToken, "refreshToken", (int) refreshTokenExpiry);
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
