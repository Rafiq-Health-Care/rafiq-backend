package com.nexaworks.rafiq.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Test {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/test")
    public String  test() {
        simpMessagingTemplate.convertAndSend("/topic/test", "test");
        return "test";
    }
    @GetMapping("/test2")
    public String  test2() {
        simpMessagingTemplate.convertAndSendToUser("8aa555a2-f0b9-4f14-adf1-b222e3af8731", "/queue/private", "test2");
        return "test2";
    }
}
