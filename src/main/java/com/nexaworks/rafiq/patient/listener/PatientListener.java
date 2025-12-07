package com.nexaworks.rafiq.patient.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.patient.service.PatientService;
import com.nexaworks.rafiq.shared.event.patient.PatientRegistrationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientListener {
    private final PatientService patientService;
    @EventListener(PatientRegistrationEvent.class)
    public void handlePatientRegistrationEvent(PatientRegistrationEvent event) {
        log.info("Patient Registration Event Received: {}", event.email());
        patientService.register(event.email(), event.firstName(), event.lastName(), event.userId());
    }
}
