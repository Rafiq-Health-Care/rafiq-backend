package com.nexaworks.rafiq.fileManagment.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.fileManagment.service.FileMetaDataService;
import com.nexaworks.rafiq.shared.event.labTest.LabTestCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralListener {
    private final FileMetaDataService fileMetaDataService;
    @Async
    @EventListener(LabTestCreatedEvent.class)
    public void handleLabTestCreatedEvent(LabTestCreatedEvent event) {
        log.info("Lab Test Created Event Received:");
        fileMetaDataService.updateFileOwner(event.fileId(), event.testId());
    }
}
