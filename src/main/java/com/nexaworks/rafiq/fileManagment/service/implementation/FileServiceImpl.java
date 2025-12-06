package com.nexaworks.rafiq.fileManagment.service.implementation;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.fileManagment.api.dto.UploadResults;
import com.nexaworks.rafiq.fileManagment.entity.File;
import com.nexaworks.rafiq.fileManagment.entity.UploadType;
import com.nexaworks.rafiq.fileManagment.exception.FileException;
import com.nexaworks.rafiq.fileManagment.repository.FileRepository;
import com.nexaworks.rafiq.fileManagment.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final CloudinaryService cloudinaryService;
    private final FileRepository fileRepository;
    @Override
    @Async
    public CompletableFuture<UUID> saveFileAsync(MultipartFile pdfFile, UUID userId) {
        String fileType = pdfFile.getContentType();
        UploadType uploadType;
        if (fileType != null && fileType.startsWith("image")) {
            uploadType = UploadType.IMAGE;
        } else if (fileType != null && fileType.startsWith("application/pdf")) {
            uploadType = UploadType.PDF;
        } else {
            throw new FileException("Unsupported file type, please upload a PDF or image file.");
        }
        UploadResults uploadResults = cloudinaryService.uploadResource(pdfFile, uploadType);
        File file = File.builder().url(uploadResults.url()).publicId(uploadResults.publicId())
                .type(fileType).name(pdfFile.getOriginalFilename()).userId(userId).build();
        return CompletableFuture.completedFuture(fileRepository.save(file).getId());
    }
}
