package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.exception.custom.MailSenderException;
import com.nexaworks.rafiq.service.ServiceImpl.EmailSenderServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;



import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;



public class EmailSenderServiceImplTest {
    @Mock
    JavaMailSender javaMailSender;
    @Mock
    SpringTemplateEngine templateEngine;
    @InjectMocks
    EmailSenderServiceImpl emailSenderService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @DisplayName("Should send email successfully")
    @Test
    void shouldSendEmailSuccessfully() {
        // Arrange
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>email</html>");

        // Act
        emailSenderService.sendEmail(Map.of("username", "Elbialy"), "test@example.com", "Test Email", "testTemplate");

        // Assert
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
        verify(templateEngine, times(1)).process(eq("testTemplate"), any(Context.class));
    }
    @DisplayName("Should throw MailSenderException on MailException")
    @Test
    void shouldThrowMailSenderExceptionOnMailException() {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>email</html>");
        doThrow(new MailException("Mail server down") {}).when(javaMailSender).send(any(MimeMessage.class));
        assertThrows(MailSenderException.class, () ->
                emailSenderService.sendEmail(
                        Map.of("username", "Elbialy"),
                        "test@example.com",
                        "Test Email",
                        "testTemplate"
                )
        );
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
        verify(templateEngine, times(1)).process(eq("testTemplate"), any(Context.class));

    }
    @DisplayName("Should throw MailSenderException on MessagingException")
    @Test
    void shouldThrowMailSenderExceptionOnMessagingException() {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> {
            throw new MessagingException("Template error");
        }).when(templateEngine).process(anyString(), any(Context.class));


        assertThrows(MailSenderException.class, () ->
                emailSenderService.sendEmail(
                        Map.of("username", "Elbialy"),
                        "test@example.com",
                        "Test Email",
                        "testTemplate"
                )
        );

        verify(templateEngine, times(1)).process(eq("testTemplate"), any(Context.class));
        verify(javaMailSender, never()).send(any(MimeMessage.class)); // should never send due to MessagingException
    }



}
