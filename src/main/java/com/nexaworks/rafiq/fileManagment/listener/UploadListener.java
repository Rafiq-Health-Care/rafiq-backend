package com.nexaworks.rafiq.fileManagment.listener;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.fileManagment.service.FileMetaDataService;
import com.nexaworks.rafiq.shared.entity.FileCategory;
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

    @Async
    @EventListener(UploadFile.class)
    public void handleUploadDoctorFiles(UploadFile uploadFile)
            throws ExecutionException, InterruptedException {
        log.info("Received UploadFile event for upload {} from file management module",
                uploadFile.category().toString());
        UUID uploadedFile = fileMetaDataService.saveFile(uploadFile.file(), uploadFile.doctorId(),
                uploadFile.category(), uploadFile.doctorId());
        publisher.publishEvent(new UploadDoctorFileSuccessfully(FileCategory.NATIONAL_ID,
                uploadFile.doctorId(), uploadedFile));
    }
}
