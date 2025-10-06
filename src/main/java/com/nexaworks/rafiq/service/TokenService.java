package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.User;

public interface TokenService {
    public String  generateRefreshToken(User user);
}
