package com.nexaworks.rafiq.fileManagment.service;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.shared.entity.FileCategory;

public interface FileMetaDataService {

    UUID saveFile(MultipartFile pdfFile, UUID ownerId, FileCategory category, UUID userId);

    void updateFileOwner(UUID uuid, UUID uuid1);
}
