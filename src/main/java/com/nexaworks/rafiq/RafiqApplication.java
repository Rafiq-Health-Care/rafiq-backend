package com.nexaworks.rafiq;

import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.Specialization;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.SpecializationRepository;
import com.nexaworks.rafiq.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "applicationAuditAware")
@EnableAsync
@EnableRetry
@EnableFeignClients
public class RafiqApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(RafiqApplication.class, args);
    }

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private SpecializationRepository specializationRepository;

    @Override
    public void run(String... args) throws Exception {
        insertRoleIfNotExists("ROLE_USER");
        insertRoleIfNotExists( "ROLE_DOCTOR");
        insertRoleIfNotExists( "ROLE_PATIENT");
        insertRoleIfNotExists( "ROLE_ADMIN");
        insertSpecializationIfNotExists("CARD", "Cardiology", "Specializes in heart and cardiovascular system disorders.");
        insertSpecializationIfNotExists("DERM", "Dermatology", "Deals with diseases of the skin, hair, and nails.");
        insertSpecializationIfNotExists("NEUR", "Neurology", "Focuses on the brain, spinal cord, and nervous system disorders.");
        insertSpecializationIfNotExists("ORTH", "Orthopedics", "Treats conditions of the bones, joints, muscles, ligaments, and tendons.");
        insertSpecializationIfNotExists("PED", "Pediatrics", "Provides medical care for infants, children, and adolescents.");
        insertSpecializationIfNotExists("PSY", "Psychiatry", "Deals with mental health, emotional, and behavioral disorders.");
        insertSpecializationIfNotExists("GYN", "Gynecology", "Focuses on the female reproductive system and related disorders.");
        insertSpecializationIfNotExists("ENT", "Otolaryngology (ENT)", "Treats conditions of the ear, nose, and throat.");
        insertSpecializationIfNotExists("URO", "Urology", "Specializes in urinary tract and male reproductive system disorders.");
        insertSpecializationIfNotExists("OPH", "Ophthalmology", "Deals with eye diseases and vision care.");
        insertSpecializationIfNotExists("GAS", "Gastroenterology", "Focuses on the digestive system and its disorders.");
        insertSpecializationIfNotExists("ONC", "Oncology", "Specializes in cancer diagnosis and treatment.");
        insertSpecializationIfNotExists("NEPH", "Nephrology", "Treats kidney and urinary system disorders.");
        insertSpecializationIfNotExists("PUL", "Pulmonology", "Specializes in lung and respiratory system disorders.");
        insertSpecializationIfNotExists("RHE", "Rheumatology", "Deals with autoimmune and musculoskeletal diseases like arthritis.");
        insertSpecializationIfNotExists("END", "Endocrinology", "Focuses on hormone-related disorders such as diabetes and thyroid issues.");
        insertSpecializationIfNotExists("GEN", "General Medicine", "Provides general medical care for adults.");
        insertSpecializationIfNotExists("SURG", "Surgery", "Performs surgical procedures for diagnosis and treatment.");
        insertSpecializationIfNotExists("EMER", "Emergency Medicine", "Provides urgent treatment for acute illnesses and injuries.");
        insertSpecializationIfNotExists("RAD", "Radiology", "Uses imaging techniques (X-ray, MRI, CT) for diagnosis and treatment.");
    }

    private void insertRoleIfNotExists(String name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }
        private void insertSpecializationIfNotExists(String code, String name, String description) {
            if (!specializationRepository.existsByCode(code)) {
                Specialization specialization = new Specialization();
                specialization.setCode(code);
                specialization.setName(name);
                specialization.setDescription(description);
                specializationRepository.save(specialization);
            }
    }
}
