package com.nexaworks.rafiq.test;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.nexaworks.rafiq.test.config.TestDataSeeder;
import com.nexaworks.rafiq.user.entity.model.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Import(TestDataSeeder.class)
public abstract class BaseIntegrationTest {
    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb").withUsername("testuser").withPassword("testpass")
            .withReuse(true);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    /**
     * Creates a SecurityMockMvcRequestPostProcessor with userId as principal
     * instead of User object. This matches the new authentication structure where
     * the principal is a UUID.
     */
    protected RequestPostProcessor withUserId(User user) {
        UUID userId = user.getId();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        Collection<SimpleGrantedAuthority> simpleAuthorities = authorities.stream()
                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                .collect(Collectors.toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null,
                simpleAuthorities);
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }
}
