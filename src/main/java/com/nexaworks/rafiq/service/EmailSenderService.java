package com.nexaworks.rafiq.service;

import java.util.Map;

public interface EmailSenderService {
    void sendEmail(Map<String, Object> model, String email, String subject, String forgetPasswordTemplate);
}
