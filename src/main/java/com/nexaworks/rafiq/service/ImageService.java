package com.nexaworks.rafiq.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    public String uploadFile(MultipartFile file) throws IOException;

}
