package com.nexaworks.rafiq.service.notification;

import java.util.Map;
import java.util.UUID;

public interface EmailContentService {
    Map<String, Object> createOtpEmail(String otp, String name, String url);

    Map<String, Object> createResetPasswordEmail(String s, String name, String url);

    Map<String, Object> createConsultationCancelledForDoctor(String doctorName, String patientName,
            UUID consultationId, String reason);

    Map<String, Object> createConsultationCancelledForPatient(String patientName, String doctorName,
            UUID consultationId, String reason);
}
