package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void changePassword(User user, String s) {
        user.setPassword(passwordEncoder.encode(s));
        userRepository.save(user);
    }
}
