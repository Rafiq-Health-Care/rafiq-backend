package com.nexaworks.rafiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.nexaworks.rafiq.idempotency.configuration.IdempotencyProperties;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "applicationAuditAware")
@EnableAsync
@EnableRetry
@EnableFeignClients
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties(IdempotencyProperties.class)

public class RafiqApplication {
    public static void main(String[] args) {
        SpringApplication.run(RafiqApplication.class, args);
    }

}
