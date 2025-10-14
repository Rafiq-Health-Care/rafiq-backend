package com.nexaworks.rafiq.service.ServiceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nexaworks.rafiq.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService implements ImageService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public List<String> uploadFile(MultipartFile file) throws IOException {


            Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return List.of( result.get("secure_url").toString(),result.get("public_id").toString());
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Collections.emptyMap());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }


}
