package com.nexaworks.rafiq.shared.event.doctor;

import java.util.UUID;

import com.nexaworks.rafiq.shared.entity.FileCategory;

public record UploadDoctorFileSuccessfully(FileCategory fileName, UUID doctorId, UUID fileId) {
}
