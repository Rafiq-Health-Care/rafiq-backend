package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.nexaworks.rafiq.exception.custom.MailSenderException;
import com.nexaworks.rafiq.service.notification.EmailSenderService;
import com.nexaworks.rafiq.service.notification.implementation.EmailSenderServiceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

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
        MimeMessage mimeMessage = new MimeMessage(
                Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>email</html>");
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        emailSenderService.sendEmail(Map.of("username", "Elbialy"), "test@example.com",
                "Test Email", "testTemplate");

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
        verify(templateEngine, times(1)).process(eq("testTemplate"), any(Context.class));
    }

    @DisplayName("Should retry if mail send exception occurs")
    @Test
    void shouldRetryIfMailExceptionOccurs() {
        MimeMessage mimeMessage = new MimeMessage(
                Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>email</html>");

        doThrow(new MailException("Simulated mail failure") {
        }).when(javaMailSender).send(any(MimeMessage.class));

        assertThrows(MailSenderException.class,
                () -> emailSenderService.sendEmail(Map.of("username", "Elbialy"),
                        "test@example.com", "Test Email", "testTemplate"));

        verify(javaMailSender, times(4)).send(any(MimeMessage.class));
    }

    @DisplayName("Should retry if mail send exception occurs")
    @Test
    void shouldThrowMailExceptionIfMessageExceptionOccurs() {
        MimeMessage mimeMessage = new MimeMessage(
                Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> {
            throw new MessagingException("Simulated message failure");
        }).when(templateEngine).process(anyString(), any(Context.class));

        assertThrows(MailSenderException.class,
                () -> emailSenderService.sendEmail(Map.of("username", "Elbialy"),
                        "test@example.com", "Test Email", "testTemplate"));

        verify(templateEngine, times(4)).process(eq("testTemplate"), any(Context.class));

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @DisplayName("Should succeed after retry on mail exception")
    @Test
    void shouldSucceedAfterRetryOnMailException() {
        MimeMessage mimeMessage = new MimeMessage(
                Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>email</html>");

        doThrow(new MailException("Simulated mail failure") {
        }).doThrow(new MailException("Simulated mail failure again") {
        }).doNothing().when(javaMailSender).send(any(MimeMessage.class));

        emailSenderService.sendEmail(Map.of("username", "Elbialy"), "test@example.com",
                "Test Email", "testTemplate");

        verify(javaMailSender, times(3)).send(any(MimeMessage.class));
        verify(templateEngine, atLeastOnce()).process(eq("testTemplate"), any(Context.class));
    }

    @DisplayName("Should succeed after retry on message exception")
    @Test
    void shouldSucceedAfterRetryOnMessageException() {
        // Prepare a MimeMessage
        MimeMessage mimeMessage = new MimeMessage(
                Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        doAnswer(invocation -> {
            throw new MessagingException("Simulated message failure");
        }).doAnswer(invocation -> {
            throw new MessagingException("Simulated message failure again");
        }).doAnswer(invocation -> "<html>email</html>").when(templateEngine).process(anyString(),
                any(Context.class));

        emailSenderService.sendEmail(Map.of("username", "Elbialy"), "test@example.com",
                "Test Email", "testTemplate");

        verify(templateEngine, times(3)).process(eq("testTemplate"), any(Context.class));
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}
