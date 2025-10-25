package com.nexaworks.rafiq.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    public List<String > uploadPhoto(MultipartFile file) throws IOException;

    void delete(String publicId);

    List<String> uploadPdf(MultipartFile file) throws IOException;
}
