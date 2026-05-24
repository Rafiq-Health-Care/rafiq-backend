package com.nexaworks.rafiq.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/user/new-otp",
                        "/api/v1/auth/refresh", "/api/v1/auth/verify",
                        "/api/v1/user/register/doctor", "/api/v1/user/register/patient",
                        "/api/v1/user/verification", "/api/v1/error", "/api/v1/specialization/**",
                        "/api/v1/v2/api-docs", "/api/v1/v3/api-docs", "/api/v1/v3/api-docs/**",
                        "/api/v1/swagger-resources", "/api/v1/swagger-resources/**",
                        "/api/v1/configuration/ui", "/api/v1/configuration/security",
                        "/api/v1/swagger-ui/**", "/api/v1/webjars/**", "/api/v1/swagger-ui.html",
                        "/api/v1/favicon.ico", "/api/v1/labs", "/api/v1/oauth2/google",
                        "/api/v1/drugs", "/api/v1/password/forget-password",
                        "/api/v1/password/change-password", "/api/v1/ws/**", "/api/v1/test2",
                        "/api/v1/index.html", "/api/v1/stripe-test.html", "/api/v1/stripe/webhook")
                .permitAll().anyRequest().authenticated())
                .sessionManagement(sc -> sc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(java.util.List.of("*"));
                    corsConfiguration.setAllowedMethods(
                            java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
                    corsConfiguration.setAllowCredentials(false);
                    corsConfiguration.setMaxAge(3600L);
                    return corsConfiguration;
                }))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
