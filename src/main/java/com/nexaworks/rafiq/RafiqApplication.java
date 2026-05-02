package com.nexaworks.rafiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "applicationAuditAware")
@EnableAsync
@EnableRetry
@EnableFeignClients
@EnableScheduling
public class RafiqApplication {
    public static void main(String[] args) {
        SpringApplication.run(RafiqApplication.class, args);
    }

}
