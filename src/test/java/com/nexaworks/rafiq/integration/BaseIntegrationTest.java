package com.nexaworks.rafiq.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.nexaworks.rafiq.client.Gemini;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.integration.config.IntegrationTestExternalMocks;
import com.nexaworks.rafiq.integration.config.TestDataSeeder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Import({TestDataSeeder.class, IntegrationTestExternalMocks.class})
public abstract class BaseIntegrationTest {

    protected static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    protected static final String MOCK_GEMINI_HTTP_RESPONSE = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{\\\"name\\\":\\\"Test Result\\\",\\\"date\\\":\\\"2024-01-15\\\",\\\"tests\\\":[]}\"}]}}]}";

    @MockBean
    protected Gemini gemini;

    @BeforeEach
    void stubGeminiClientApi() {
        when(gemini.getResult(any())).thenReturn(MOCK_GEMINI_HTTP_RESPONSE);
    }

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

    protected RequestPostProcessor withUserId(User user) {
        UUID userId = user.getId();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        Collection<SimpleGrantedAuthority> simpleAuthorities = authorities.stream()
                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                .collect(Collectors.toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null,
                simpleAuthorities);
        return request -> {
            if (request.getHeader(IDEMPOTENCY_KEY_HEADER) == null) {
                request.addHeader(IDEMPOTENCY_KEY_HEADER, UUID.randomUUID().toString());
            }
            return SecurityMockMvcRequestPostProcessors.authentication(authentication)
                    .postProcessRequest(request);
        };
    }
    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.rabbitmq.host", rabbit::getHost);
        r.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        r.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        r.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }
}
