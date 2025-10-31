package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.exception.custom.MailSenderException;
import com.nexaworks.rafiq.service.ServiceImpl.EmailSenderServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("EmailSenderServiceImpl Test Cases")
@SpringBootTest
@EnableRetry
@ContextConfiguration(classes = {EmailSenderServiceImpl.class})
public class EmailSenderServiceImplTest {

    @MockitoBean
    JavaMailSender javaMailSender;

    @MockitoBean
    SpringTemplateEngine templateEngine;

    @Autowired
    EmailSenderService emailSenderService;

    @DisplayName("Should send email successfully")
    @Test
    void shouldSendEmailSuccessfully() {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>email</html>");
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        emailSenderService.sendEmail(
                Map.of("username", "Elbialy"),
                "test@example.com",
                "Test Email",
                "testTemplate"
        );

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
        verify(templateEngine, times(1)).process(eq("testTemplate"), any(Context.class));
    }
    @DisplayName("Should retry if mail send exception occurs")
    @Test
    void shouldRetryIfMailExceptionOccurs() {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>email</html>");

        doThrow(new MailException("Simulated mail failure") {})
                .when(javaMailSender)
                .send(any(MimeMessage.class));

        // Act + Assert
        assertThrows(MailSenderException.class, () ->
                emailSenderService.sendEmail(
                        Map.of("username", "Elbialy"),
                        "test@example.com",
                        "Test Email",
                        "testTemplate"
                ));

        verify(javaMailSender, times(4)).send(any(MimeMessage.class));
    }
    @DisplayName("Should retry if mail send exception occurs")
    @Test
    void shouldRetryIfMessageExceptionOccurs()  {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> {
            throw new MessagingException("Simulated message failure");
        }).when(templateEngine).process(anyString(), any(Context.class));


        assertThrows(MailSenderException.class, () ->
                emailSenderService.sendEmail(
                        Map.of("username", "Elbialy"),
                        "test@example.com",
                        "Test Email",
                        "testTemplate"
                ));

        verify(templateEngine, times(4))
                .process(eq("testTemplate"), any(Context.class));

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }



}
