package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.dto.request.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.VerifyOtpRequest;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.UserNotFoundException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    public static final String SUBJECT = "Reset password";
    public static final String FORGET_PASSWORD_TEMPLATE = "forget-password.html";
    public static final String URL = "http://localhost:8032/auth/verfiy";
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailContentService emailContentService;
    private final EmailSenderService emailSenderService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public void forgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        String email = forgetPasswordRequest.email();
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException("User with email " + email + " not found"));
        String otp = tokenService.generateOtpToken(user);
        log.info("Generated OTP {}",otp);
        // todo send otp to user via email
        Map<String,Object> model = emailContentService.createOtpEmail(otp,user.getName(),URL);
        emailSenderService.sendEmail(model,email,SUBJECT,FORGET_PASSWORD_TEMPLATE);
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        Token otp = tokenService.getToken(verifyOtpRequest.otp());
        if (!otp.getUser().getEmail().equals(verifyOtpRequest.email())
                ||otp.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        String accessToken = tokenService.generateAccessToken
                (userService.findByEmail(verifyOtpRequest.email()));
        return new VerifyOtpResponse(accessToken);
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Token token = tokenService.getToken(changePasswordRequest.accessToken());
        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid Access Token");
        }
        User user = token.getUser();
        userService.changePassword(user,changePasswordRequest.newPassword());
        log.info("Password changed for user {}",user.getEmail());
    }

    @Override
    public LoginResponse login(String email, String password) {
        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(email,password));
        User user = (User) authentication.getPrincipal();
        String jwt = jwtService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        return new LoginResponse(
               user.getRoles().stream().map(Role::getName).toList(),jwt,refreshToken
        );

    }

    private void authenticateUser(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,null,user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
