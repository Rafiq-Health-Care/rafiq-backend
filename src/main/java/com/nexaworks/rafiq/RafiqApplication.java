package com.nexaworks.rafiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "applicationAuditAware")
public class RafiqApplication {
    public static void main(String[] args) {
        SpringApplication.run(RafiqApplication.class, args);
    }
}
