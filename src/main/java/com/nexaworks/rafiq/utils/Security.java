package com.nexaworks.rafiq.utils;

import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.service.JwtService;
import com.nexaworks.rafiq.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Security {
    private final JwtService jwtService;
    private final TokenService tokenService;

    @NotNull
    public LoginResponse createLoginSession(HttpServletResponse response, User user) {
        String jwt = jwtService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        addTokenToCookie(response,jwt,"jwt",60*60*24);
        addTokenToCookie(response,refreshToken,"refreshToken",60*60*24*30);
        return new LoginResponse(
                user.getRoles().stream().map(Role::getName)
                        .filter(role -> !role.equals("ROLE_USER")).findFirst());
    }
    public void addTokenToCookie(HttpServletResponse response,String token,String cookieName,int expiry){
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge( expiry);
        response.addCookie(cookie);
    }
}
