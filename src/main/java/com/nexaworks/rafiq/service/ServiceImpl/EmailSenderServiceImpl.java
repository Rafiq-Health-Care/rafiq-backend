package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    // todo add retry logic
    @Async
    @Override
    @Retryable(
            retryFor = {MailException.class,MessagingException.class},
            maxAttempts = 4,
            backoff = @Backoff(delay = 10000)
    )
    public void sendEmail(Map<String, Object> model, String email, String subject, String forgetPasswordTemplate) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        try{
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setFrom("rafiq@rafig.com");
            mimeMessageHelper.setTo(email);
            Context context = new Context();
            context.setVariables(model);
            String text = templateEngine.process(forgetPasswordTemplate,context);
            mimeMessageHelper.setText(text,true);
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully");

        }catch (MailException | MessagingException e){
            log.error("Error sending email: {}",e.getMessage());
        }

    }

}
