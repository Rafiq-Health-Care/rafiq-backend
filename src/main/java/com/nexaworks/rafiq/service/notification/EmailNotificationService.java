package com.nexaworks.rafiq.service.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.nexaworks.rafiq.exception.custom.general.MailSenderException;
import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("email")
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService<EmailNotification> {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    @Retryable(retryFor = {MailSenderException.class,
            MessagingException.class}, maxAttempts = 4, backoff = @Backoff(delay = 10000))
    public void sendNotification(EmailNotification notificationDetails) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        try {
            mimeMessageHelper.setSubject(notificationDetails.subject());
            mimeMessageHelper.setFrom("rafiq@rafig.com");
            mimeMessageHelper.setTo(notificationDetails.email());
            Context context = new Context();
            context.setVariables(notificationDetails.variables());
            String text = templateEngine.process(notificationDetails.template(), context);
            mimeMessageHelper.setText(text, true);
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully");

        } catch (MailException | MessagingException e) {
            throw new MailSenderException("Failed to send email");
        }
    }
}
