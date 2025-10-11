package com.nexaworks.rafiq.service.ServiceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nexaworks.rafiq.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService implements ImageService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {

            return  cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap())
                    .get("public_id").toString();
    }

    public String generateSignedUrl(String publicId) {
        long timestamp = System.currentTimeMillis() / 1000L;

        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "timestamp", timestamp,
                "resource_type", "image"
        );

        String signature = cloudinary.apiSignRequest(params, (String) cloudinary.config.apiSecret);

        return String.format(
                "https://res.cloudinary.com/%s/image/upload/s--%s--/%s.jpg",
                cloudinary.config.cloudName,
                signature,
                publicId
        );
    }
}
