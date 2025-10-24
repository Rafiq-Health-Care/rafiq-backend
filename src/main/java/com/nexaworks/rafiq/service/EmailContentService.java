package com.nexaworks.rafiq.service;

import java.util.Map;

public interface EmailContentService {
    Map<String, Object> createOtpEmail(String otp, String name, String url);

}
