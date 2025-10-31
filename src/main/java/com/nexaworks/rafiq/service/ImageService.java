package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.UploadResults;
import com.nexaworks.rafiq.enums.UploadType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    UploadResults uploadResource(MultipartFile file, UploadType type) throws IOException;

    void delete(String publicId);

}
