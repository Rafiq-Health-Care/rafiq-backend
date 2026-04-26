package com.nexaworks.rafiq.integration.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.SpecializationRepository;

@TestConfiguration
@Profile("test")
public class TestDataSeeder {

    @Bean
    public ApplicationRunner initializeTestData(RoleRepository roleRepository,
            SpecializationRepository specializationRepository) {
        return args -> {
            // Insert Roles
            insertRoles(roleRepository);

            // Insert Specializations
            insertSpecializations(specializationRepository);
        };
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

    private void insertSpecializations(SpecializationRepository specializationRepository) {
        createSpecializationIfNotExists(specializationRepository, "Cardiology",
                "Cardiovascular diseases and heart conditions", "CARD");
        createSpecializationIfNotExists(specializationRepository, "Neurology",
                "Nervous system disorders and brain conditions", "NEURO");
        createSpecializationIfNotExists(specializationRepository, "Pediatrics",
                "Medical care for infants, children, and adolescents", "PED");
        createSpecializationIfNotExists(specializationRepository, "General Medicine",
                "General medical practice and primary care", "GEN");
        createSpecializationIfNotExists(specializationRepository, "Orthopedics",
                "Musculoskeletal system, bones, joints, and muscles", "ORTHO");
        createSpecializationIfNotExists(specializationRepository, "Dermatology",
                "Skin, hair, and nail conditions", "DERM");
        createSpecializationIfNotExists(specializationRepository, "Ophthalmology",
                "Eye and vision care", "OPHTH");
        createSpecializationIfNotExists(specializationRepository, "ENT",
                "Ear, nose, and throat conditions", "ENT");
        createSpecializationIfNotExists(specializationRepository, "Psychiatry",
                "Mental health and psychiatric disorders", "PSYCH");
        createSpecializationIfNotExists(specializationRepository, "Gynecology",
                "Women's reproductive health", "GYN");
        createSpecializationIfNotExists(specializationRepository, "Urology",
                "Urinary tract and male reproductive system", "URO");
        createSpecializationIfNotExists(specializationRepository, "Oncology",
                "Cancer diagnosis and treatment", "ONCO");
        createSpecializationIfNotExists(specializationRepository, "Gastroenterology",
                "Digestive system disorders", "GI");
        createSpecializationIfNotExists(specializationRepository, "Pulmonology",
                "Respiratory system and lung conditions", "PULM");
        createSpecializationIfNotExists(specializationRepository, "Endocrinology",
                "Hormone and metabolic disorders", "ENDO");
    }

    private void createSpecializationIfNotExists(SpecializationRepository specializationRepository,
            String name, String description, String code) {
        if (specializationRepository.findByCode(code).isEmpty()) {
            Specialization specialization = Specialization.builder().name(name)
                    .description(description).code(code).build();
            specializationRepository.save(specialization);
        }
    }
}
