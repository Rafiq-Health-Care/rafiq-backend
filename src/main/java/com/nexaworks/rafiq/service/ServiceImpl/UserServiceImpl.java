package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.dto.request.DoctorRegistrationRequest;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.RegistrationException;
import com.nexaworks.rafiq.mapper.UserMapper;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.DoctorService;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.service.RoleService;
import com.nexaworks.rafiq.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        PatientProfile patientProfile = patientService.createPatientProfile(patient);
        patient.setPatientProfile(patientProfile);
        userRepository.save(patient);
        log.info("User registered {}",user.getEmail());

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
        doctor.setRoles(List.of(roleService.getRole(ROLE_USER),roleService.getRole(ROLE_DOCTOR)));
        doctor.setPatientProfile(patientService.createPatientProfile(doctor));
        doctor.setDoctorProfile(doctorService.createProfile(doctor));
        userRepository.save(doctor);



    }
}
