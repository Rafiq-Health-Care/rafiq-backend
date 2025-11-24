package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.service.EmailContentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailContentServiceImpl implements EmailContentService {

    @Override
    public Map<String, Object> createOtpEmail(String otp, String name, String url) {
        return Map.of("otp", otp, "name", name, "url", url);
    }

    @Override
    public Map<String, Object> createResetPasswordEmail(String accessToken, String name,
            String url) {
        return Map.of("name", name, "url", url + "?token=" + accessToken);
    }
}
