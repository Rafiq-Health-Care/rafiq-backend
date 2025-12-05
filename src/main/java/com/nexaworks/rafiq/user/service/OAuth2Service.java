package com.nexaworks.rafiq.user.service;

import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface OAuth2Service {
    LoginResponse oAuth2(String idToken, HttpServletResponse response);
}
