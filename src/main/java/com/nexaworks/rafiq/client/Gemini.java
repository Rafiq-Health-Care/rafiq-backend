package com.nexaworks.rafiq.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nexaworks.rafiq.dto.client.RequestBodyDTO;

@Component
@FeignClient(name = "gemini-client", url = "https://generativelanguage.googleapis.com/v1beta/models")
public interface Gemini {
    @PostMapping(value = "/gemini-2.5-flash:generateContent?key=${spring.ai.google.genai.api-key}", consumes = "application/json")
    String getResult(@RequestBody RequestBodyDTO body);
}
