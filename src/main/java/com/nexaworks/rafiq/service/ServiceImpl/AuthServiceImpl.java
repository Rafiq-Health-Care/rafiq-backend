package com.nexaworks.rafiq.service.ServiceImpl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.nexaworks.rafiq.dto.event.ForgetPasswordEvent;
import com.nexaworks.rafiq.dto.request.*;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.*;
import com.nexaworks.rafiq.utils.Security;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;
    private final Security  security;

    @Override
    @Transactional
    public void forgetPassword(ForgetPasswordRequest forgetPasswordRequest) {
        String email = forgetPasswordRequest.email();
        User user = userService.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException("User with email " + email + " not found"));
        String otp = tokenService.generateOtpToken(user);
        log.info("Generated OTP {}",otp);
        eventPublisher.publishEvent(
                new ForgetPasswordEvent(email,otp,user.getFirstName()));
    }

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        validateToken(verifyOtpRequest);
        String accessToken = tokenService.generateAccessToken
                (userService.findByEmail(verifyOtpRequest.email()));
        return new VerifyOtpResponse(accessToken);
    }

    private void validateToken(VerifyOtpRequest verifyOtpRequest) {
        Token otp = tokenService.getToken(verifyOtpRequest.otp());
        if (!otp.getUser().getEmail().equals(verifyOtpRequest.email())
                ||otp.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Token token = tokenService.getToken(changePasswordRequest.accessToken());
        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenInvalidException("Invalid Access Token");
        }
        User user = token.getUser();
        userService.changePassword(user,changePasswordRequest.newPassword());
        log.info("Password changed for user {}",user.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = getAuthenticateUser();
        userService.updatePassword(user,resetPasswordRequest);


    }

    public User getAuthenticateUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional
    public LoginResponse login(String email, String password, HttpServletResponse response) {
        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(email,password));
        User user = (User) authentication.getPrincipal();
        return security.createLoginSession(response, user);

    }
    @Override
    @Transactional
    public LoginResponse refresh(HttpServletResponse response, HttpServletRequest request) {
        Token token = tokenService.getToken(security.getCookie(request, "refreshToken"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenInvalidException("Invalid Refresh Token");
        }
        User user = token.getUser();
        return  security.createLoginSession(response, user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request,HttpServletResponse response) {
        tokenService.invalidateRefreshToken(tokenService.getToken(request.refreshToken()));
        jwtService.invalidateJwtToken(request.jwtToken());
        removeJwtFromCookies(response);

    }

    @Override
    @Transactional
    public LoginResponse oAuth2(String idToken,HttpServletResponse response) throws GeneralSecurityException, IOException {
        GoogleIdToken googleIdToken = getGoogleIdToken(idToken);
        if (googleIdToken!=null){
            String email = googleIdToken.getPayload().getEmail();
            String firstName = googleIdToken.getPayload().get("given_name").toString();
            String lastName = googleIdToken.getPayload().get("family_name").toString();
            Optional<User> user = getUser(email, firstName, lastName);
            if (user.isPresent()) {
                return  security.createLoginSession(response, user.get());
            }
            else {
                throw new UserNotFoundException("User not found");
            }
        }
        else {
            throw new TokenInvalidException("Invalid id token");
        }


    }

    @NotNull
    private Optional<User> getUser(String email, String firstName, String lastName) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isPresent()){
            User existingUser = user.get();
            if (!existingUser.isEnabled()){
                existingUser.setEnabled(true);
                userRepository.save(existingUser);
            }
        }
        else {
            user = Optional.ofNullable(userService.addUser(email, firstName, lastName));
        }
        return user;
    }

    private GoogleIdToken getGoogleIdToken(String idToken) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new JacksonFactory()
        ).setAudience(Collections.singleton(clientId)).build();
        GoogleIdToken googleIdToken = verifier.verify(idToken);
        return googleIdToken;
    }

    private void removeJwtFromCookies(HttpServletResponse response ){
        Cookie cookie = new Cookie("jwt",null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
