package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.dto.UploadResults;
import com.nexaworks.rafiq.dto.event.NewOtpEvent;
import com.nexaworks.rafiq.dto.event.UserRegistrationEvent;
import com.nexaworks.rafiq.dto.request.ResetPasswordRequest;

import com.nexaworks.rafiq.dto.request.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;


import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.TokenType;
import com.nexaworks.rafiq.enums.UploadType;
import com.nexaworks.rafiq.exception.custom.RegistrationException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.mapper.UserMapper;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.nexaworks.rafiq.enums.Roles.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final TokenService tokenService;
    private final EmailSenderService emailSenderService;
    private final EmailContentService emailContentService;
    private final JwtService jwtService;
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void changePassword(User user, String s) {
        user.setPassword(passwordEncoder.encode(s));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(User user, ResetPasswordRequest resetPasswordRequest) {
        if(!passwordEncoder.matches(resetPasswordRequest.oldPassword(),user.getPassword())){
            throw new IllegalArgumentException("Old password is not correct");
        }
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.newPassword()));
        userRepository.save(user);
        log.info("Password updated for user {}",user.getEmail());

    }
    @Override
    @Transactional
    public void registerPatient(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RegistrationException("User with email " + user.getEmail() + " already exists");
        }
        User patient = extracted(user);
        userRepository.save(patient);
        PatientProfile patientProfile = patientService.createPatientProfile(patient);
        patient.setPatientProfile(patientProfile);
        log.info("User registered {}",user.getEmail());
       String otp =  tokenService.generateOtpToken(patient);
       log.info("OTP generated {}",otp);
        eventPublisher.publishEvent(
                new UserRegistrationEvent(user.getEmail(),otp,user.getFirstName()));
    }

    private User extracted(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleService.getRole(ROLE_USER);
        Role role1 = roleService.getRole(ROLE_PATIENT);
        user.setRoles(List.of(role,role1));
        return user;
    }

    @Override
    @Transactional
    public void registerDoctor(DoctorRegistrationRequest request, MultipartFile nationalId) throws IOException {
        User user = UserMapper.toUser(request.user());
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RegistrationException("User with email " + user.getEmail() + " already exists");
        }
        User doctor = extracted(user);
        userRepository.save(doctor);
       UploadResults nationalIdImage = imageService.uploadResource(nationalId, UploadType.IMAGE);
        doctor.setRoles(List.of(roleService.getRole(ROLE_USER),roleService.getRole(ROLE_DOCTOR)));
        doctor.setDoctorProfile(doctorService.createProfile(doctor,request.description(),request.specialization(),nationalIdImage.url(),nationalIdImage.publicId()));
       String otp = tokenService.generateOtpToken(doctor);
       log.info("OTP generated {}",otp);
        eventPublisher.publishEvent(
                new UserRegistrationEvent(user.getEmail(),otp,user.getFirstName()));

    }

    @Override
    @Transactional
    public LoginResponse verifyOtp(String email, String otp, HttpServletResponse response) {
     User user = tokenService.verifyOtp(email,otp);
     user.setEnabled(true);
     userRepository.save(user);
     String jwt = jwtService.generateToken(user);
     addJwtToCookie(response,jwt);
     String refreshToken = tokenService.generateRefreshToken(user);
     return new LoginResponse(user.getRoles().stream().map(Role::getName).toList(),refreshToken);
    }

    private void addJwtToCookie(HttpServletResponse response, String jwt) {
        Cookie cookie = new Cookie("jwt",jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60*60*24);
        response.addCookie(cookie);
    }

    @Override
    @Transactional
    public void getNewOtp(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new UserNotFoundException("User with email " + email + " not found"));
        Optional<Token> otpToken = user.getTokens().stream().filter(token ->
                token.getTokenType().equals(TokenType.OTP)&&
                token.getExpiryDate().isAfter(Instant.now())).findFirst();
        otpToken.ifPresent(token -> token.setExpiryDate(Instant.now()));
        String otp  = tokenService.generateOtpToken(user);
        log.info("New OTP generated {}",otp);
        eventPublisher.publishEvent(
                new NewOtpEvent(user.getEmail(),otp,user.getFirstName()));

    }

    @Override
    public User getUser() {

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional
    public User addUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(false);
        user.setRoles(List.of(roleService.getRole(ROLE_USER)));
        User oAuthUser = userRepository.save(user);
        log.info("User created {}",user.getEmail());
        return oAuthUser;

    }



}
