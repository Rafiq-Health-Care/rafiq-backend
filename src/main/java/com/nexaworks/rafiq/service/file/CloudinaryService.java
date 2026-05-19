package com.nexaworks.rafiq.service.file;

import java.io.IOException;
import java.util.Collections;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.enums.UploadType;
import com.nexaworks.rafiq.exception.custom.file.EmptyFileException;
import com.nexaworks.rafiq.exception.custom.file.FileException;
import com.nexaworks.rafiq.exception.custom.file.FileUploadException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService implements ImageService {
    private final Cloudinary cloudinary;

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
