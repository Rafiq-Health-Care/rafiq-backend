package com.nexaworks.rafiq.shared.event.doctor;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.shared.entity.FileCategory;

public record UploadFile(MultipartFile file, UUID doctorId, FileCategory category) {
}
