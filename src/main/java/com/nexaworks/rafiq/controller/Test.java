package com.nexaworks.rafiq.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.service.call.RtcProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class Test {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RtcProvider rtcProvider;

    @GetMapping("/test")
    public String test() {
        simpMessagingTemplate.convertAndSend("/topic/test", "test");
        return "test";
    }
    @GetMapping("/test2")
    public String test2() {
        return rtcProvider.generateToken("test", 1000);
    }

}
