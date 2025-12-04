package com.nexaworks.rafiq.service.authentication;

import com.nexaworks.rafiq.dto.response.auth.LoginResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface OAuth2Service {
    LoginResponse oAuth2(String idToken, HttpServletResponse response);
}
