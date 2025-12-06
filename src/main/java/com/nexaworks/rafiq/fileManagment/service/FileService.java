package com.nexaworks.rafiq.fileManagment.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    CompletableFuture<UUID> saveFileAsync(MultipartFile pdfFile, UUID patientId);
}
