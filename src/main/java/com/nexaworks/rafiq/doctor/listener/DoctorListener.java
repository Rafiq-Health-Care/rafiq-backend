package com.nexaworks.rafiq.doctor.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.doctor.service.DoctorService;
import com.nexaworks.rafiq.shared.event.doctor.DoctorRegisterEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DoctorListener {
    private final DoctorService doctorService;
    @EventListener(DoctorRegisterEvent.class)
    public void handleDoctorRegistrationEvent(DoctorRegisterEvent event) {
        log.info("Doctor Registration Event Received: {}", event.basicInfo().email());
        doctorService.register(event);
    }
}
