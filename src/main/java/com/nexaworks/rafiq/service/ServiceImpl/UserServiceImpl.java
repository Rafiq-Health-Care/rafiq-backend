package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.dto.request.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.RegistrationException;
import com.nexaworks.rafiq.mapper.UserMapper;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void changePassword(User user, String s) {
        user.setPassword(passwordEncoder.encode(s));
        userRepository.save(user);
    }

    @Override
    public void registerPatient(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RegistrationException("User with email " + user.getEmail() + " already exists");
        }
        User patient = extracted(user);
        userRepository.save(patient);
        PatientProfile patientProfile = patientService.createPatientProfile(patient);
        patient.setPatientProfile(patientProfile);
        log.info("User registered {}",user.getEmail());
        generateOtpAndSendEmail(user);

    }

    private User extracted(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleService.getRole(ROLE_USER);
        Role role1 = roleService.getRole(ROLE_PATIENT);
        user.setRoles(List.of(role,role1));
        return user;
    }

    @Override
    public void registerDoctor(DoctorRegistrationRequest request) {
        User user = UserMapper.toUser(request.user());
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RegistrationException("User with email " + user.getEmail() + " already exists");
        }
        User doctor = extracted(user);
        userRepository.save(doctor);
        doctor.setRoles(List.of(roleService.getRole(ROLE_USER),roleService.getRole(ROLE_DOCTOR),roleService.getRole(ROLE_PATIENT)));
        doctor.setPatientProfile(patientService.createPatientProfile(doctor));
        doctor.setDoctorProfile(doctorService.createProfile(doctor,request.description(),request.specialization()));
        generateOtpAndSendEmail(user);
    }

    @Override
    public LoginResponse verifyOtp(String email, String otp) {
     User user = tokenService.verifyOtp(email,otp);
     user.setEnabled(true);
     userRepository.save(user);
     String jwt = jwtService.generateToken(user);
     String refreshToken = tokenService.generateRefreshToken(user);
     return new LoginResponse(user.getRoles().stream().map(Role::getName).toList(),jwt,refreshToken);
    }

    private void generateOtpAndSendEmail(User user) {
        String otpToken = tokenService.generateOtpToken(user);
        Map<String ,Object> model = emailContentService.createOtpEmail(otpToken,user.getName(),"url");
        emailSenderService.sendEmail(model,
                user.getEmail(),"Verify your email address",
                "OTP_TEMPLATE.html");
    }
}
