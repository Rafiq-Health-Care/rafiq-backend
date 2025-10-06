package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
