package com.nexaworks.rafiq.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;
    private long limit = 120;
    private long windowSeconds = 60;
    private List<String> paths = new ArrayList<>(List.of("/api/v1/auth/**", "/api/v1/password/**",
            "/api/v1/oauth2/**", "/api/v1/drugs/**", "/api/v1/doctors/search"));
}
