package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.dto.ForgetPasswordRequest;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.UserNotFoundException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.AuthService;
import com.nexaworks.rafiq.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    public void forgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        String email = forgetPasswordRequest.email();
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException("User with email " + email + " not found"));
        authenticateUser(user);
        String otp = tokenService.generateOtpToken(user);
        log.info("Generated OTP {}",otp);
        // todo send otp to user via email


    }

    private void authenticateUser(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,null,user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
