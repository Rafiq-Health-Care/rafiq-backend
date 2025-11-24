package com.nexaworks.rafiq.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.enums.UploadType;

public interface ImageService {
    UploadResults uploadResource(MultipartFile file, UploadType type) throws IOException;

    void delete(String publicId);
}
