package com.nexaworks.rafiq.integration.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.repository.RoleRepository;

@TestConfiguration
@Profile("test")
public class TestDataSeeder {

    @Bean
    public ApplicationRunner initializeTestData(RoleRepository roleRepository) {
        return args -> insertRoles(roleRepository);
    }

    private void insertRoles(RoleRepository roleRepository) {
        createRoleIfNotExists(roleRepository, "ROLE_DOCTOR");
        createRoleIfNotExists(roleRepository, "ROLE_PATIENT");
        createRoleIfNotExists(roleRepository, "ROLE_USER");
        createRoleIfNotExists(roleRepository, "ROLE_ADMIN");
    }

    private void createRoleIfNotExists(RoleRepository roleRepository, String roleName) {
        if (roleRepository.findByName(roleName) == null) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}
