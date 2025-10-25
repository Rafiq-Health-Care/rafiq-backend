package com.nexaworks.rafiq.service.ServiceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nexaworks.rafiq.exception.custom.EmptyFileException;
import com.nexaworks.rafiq.exception.custom.FileUploadException;
import com.nexaworks.rafiq.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService implements ImageService {
    private final Cloudinary cloudinary;


    public UploadResults uploadPhoto(MultipartFile file,UploadType type) throws IOException {
        if (file.isEmpty()) {
            throw new EmptyFileException("File is empty");
        }
        try (var inputStream = file.getInputStream()){
            var map = cloudinary.uploader().upload(
                            inputStream,
                            ObjectUtils.asMap(
                                    "resource_type", "auto"
                            )
                    );
            return List.of(map.get("secure_url").toString(),map.get("public_id").toString());

        } catch (IOException e) {
            throw new FileUploadException("Filed to upload the file, please try again");
        }

    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Collections.emptyMap());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public List<String> uploadPdf(MultipartFile file) throws IOException {
        Map<String ,Object> map =
                cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "resource_type", "raw",
                                "folder", "pdf"
                        )
                );
        return List.of(map.get("secure_url").toString(),map.get("public_id").toString());

    }


}
