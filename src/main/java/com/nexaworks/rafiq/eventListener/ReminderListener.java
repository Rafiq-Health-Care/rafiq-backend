package com.nexaworks.rafiq.eventListener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.event.ReminderEvent;
import com.nexaworks.rafiq.service.medicine.ReminderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReminderListener {
    private final ReminderService reminderService;

    @EventListener(ReminderEvent.class)
    public void reminderEvent(ReminderEvent event) {
        log.info("Received reminder event");
    }
}
