package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.service.EmailContentService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailContentServiceImpl implements EmailContentService {

    @Override
    public Map<String, Object> createOtpEmail(String otp, String name, String url) {
        return Map.of("otp", otp, "name", name, "url", url);
    }
}
