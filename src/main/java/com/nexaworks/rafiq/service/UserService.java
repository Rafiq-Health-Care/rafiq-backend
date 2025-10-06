package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.User;

import java.util.Optional;

public interface UserService {
    public Optional<User> findByEmail(String email);
}
