package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.User;

public interface JwtService {

    String generateToken(User user);
}
