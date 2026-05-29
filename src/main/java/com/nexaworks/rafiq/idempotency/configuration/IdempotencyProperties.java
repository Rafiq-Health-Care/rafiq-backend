package com.nexaworks.rafiq.idempotency.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
@ConfigurationProperties(prefix = "idempotency")
public class IdempotencyProperties {
    private String headerName = "Idempotency-Key";

    private Duration ttl = Duration.ofHours(10);

    private boolean enabled = true;

}
