package com.nexaworks.rafiq.fileManagment.listener;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.fileManagment.service.FileMetaDataService;
import com.nexaworks.rafiq.shared.event.doctor.UploadDoctorFileSuccessfully;
import com.nexaworks.rafiq.shared.event.doctor.UploadFile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class UploadListener {
    private final FileMetaDataService fileMetaDataService;
    private final ApplicationEventPublisher publisher;

    @EventListener(UploadFile.class)
    public void handleUploadDoctorFiles(UploadFile uploadFile) {
        log.info("Received UploadFile event for upload {} from file management module",
                uploadFile.category().toString());
        UUID fileId = fileMetaDataService.saveFile(uploadFile.file(), uploadFile.doctorId(),
                uploadFile.category(), uploadFile.doctorId());
        log.info("File saved successfully");
        publisher.publishEvent(new UploadDoctorFileSuccessfully(uploadFile.file().getName(),
                uploadFile.doctorId(), fileId));
    }
}
