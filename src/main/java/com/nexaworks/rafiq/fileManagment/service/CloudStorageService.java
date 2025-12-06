package com.nexaworks.rafiq.fileManagment.service;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.fileManagment.api.dto.UploadResults;
import com.nexaworks.rafiq.fileManagment.entity.UploadType;

public interface CloudStorageService {
    UploadResults uploadResource(MultipartFile file, UploadType type);
    void delete(String publicId);
}
