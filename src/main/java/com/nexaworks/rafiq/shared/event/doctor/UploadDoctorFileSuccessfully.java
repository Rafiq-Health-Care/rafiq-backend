package com.nexaworks.rafiq.shared.event.doctor;

import java.util.UUID;

public record UploadDoctorFileSuccessfully(String fileName, UUID doctorId, UUID fileId) {
}
