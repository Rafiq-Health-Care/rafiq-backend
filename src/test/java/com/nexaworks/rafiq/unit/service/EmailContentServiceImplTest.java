package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nexaworks.rafiq.service.notification.EmailContentServiceImpl;

@DisplayName("EmailContentService Test Cases")
public class EmailContentServiceImplTest {
    @DisplayName("Should Return OTP Email Content")
    @Test
    void shouldReturnOtpEmailContent() {
        EmailContentServiceImpl emailContentService = new EmailContentServiceImpl();
        Map<String, Object> emailContent = emailContentService.createOtpEmail("123456", "Nexa",
                "http://localhost:8080/api/v1/auth/verify-otp?otp=123456");
        assertThat(emailContent).isNotNull();
        assertThat(emailContent.get("otp")).isEqualTo("123456");
        assertThat(emailContent.get("name")).isEqualTo("Nexa");
        assertThat(emailContent.get("url"))
                .isEqualTo("http://localhost:8080/api/v1/auth/verify-otp?otp=123456");
    }
}
