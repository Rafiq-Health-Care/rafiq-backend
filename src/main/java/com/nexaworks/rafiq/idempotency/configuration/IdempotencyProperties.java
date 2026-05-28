package com.nexaworks.rafiq.idempotency.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "idempotency")
public class IdempotencyProperties {
    private String headerName = "Idempotency-Key";

    private Duration ttl = Duration.ofHours(10);

    private boolean enabled = true;

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
