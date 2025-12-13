package com.nexaworks.rafiq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.nexaworks.rafiq.test.BaseIntegrationTest;

@SpringBootTest
@ActiveProfiles("test")
class RafiqApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
    }
}