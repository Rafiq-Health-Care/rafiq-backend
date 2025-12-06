package com.nexaworks.rafiq.fileManagment.service.implementation;

import java.io.IOException;
import java.util.Collections;

import com.nexaworks.rafiq.fileManagment.service.CloudStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nexaworks.rafiq.fileManagment.api.dto.UploadResults;
import com.nexaworks.rafiq.fileManagment.entity.UploadType;
import com.nexaworks.rafiq.fileManagment.exception.EmptyFileException;
import com.nexaworks.rafiq.fileManagment.exception.FileException;
import com.nexaworks.rafiq.fileManagment.exception.FileUploadException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService implements CloudStorageService {
    private final Cloudinary cloudinary;

    @Override
    public UploadResults uploadResource(MultipartFile file, UploadType type) {
        if (file.isEmpty()) {
            throw new EmptyFileException("File is empty");
        }
        try {
            var map = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", type.getCloudinaryType()));

            return new UploadResults(map.get("secure_url").toString(),
                    map.get("public_id").toString());

        } catch (IOException e) {
            throw new FileUploadException("Filed to upload the file, please try again");
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Collections.emptyMap());
        } catch (Exception e) {
            throw new FileException("Filed to delete the file, please try again");
        }
    }


}
