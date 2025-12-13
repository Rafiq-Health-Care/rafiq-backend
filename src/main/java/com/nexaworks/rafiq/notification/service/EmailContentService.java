package com.nexaworks.rafiq.notification.service;

import java.util.Map;

public interface EmailContentService {
    Map<String, Object> createOtpEmail(String otp, String name, String url);

    Map<String, Object> createResetPasswordEmail(String s, String name, String url);
}
