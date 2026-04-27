package com.nexaworks.rafiq.service.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailContentServiceImpl implements EmailContentService {

    @Override
    public Map<String, Object> createOtpEmail(String otp, String name, String url) {
        return Map.of("otp", otp, "name", name, "url", url);
    }

    @Override
    public Map<String, Object> createResetPasswordEmail(String accessToken, String name,
            String url) {
        return Map.of("name", name, "url", url + "?token=" + accessToken);
    }

    @Override
    public Map<String, Object> createConsultationCancelledForDoctor(String doctorName,
            String patientName, UUID consultationId, String reason) {
        Map<String, Object> model = new HashMap<>();
        model.put("recipientName", safeName(doctorName, "Doctor"));
        model.put("otherPartyName", safeName(patientName, "The patient"));
        model.put("consultationId", consultationId.toString());
        model.put("reason", reason != null ? reason : "No reason provided");
        return model;
    }

    @Override
    public Map<String, Object> createConsultationCancelledForPatient(String patientName,
            String doctorName, UUID consultationId, String reason) {
        Map<String, Object> model = new HashMap<>();
        model.put("recipientName", safeName(patientName, "Patient"));
        model.put("otherPartyName", safeName(doctorName, "Your doctor"));
        model.put("consultationId", consultationId.toString());
        model.put("reason", reason != null ? reason : "No reason provided");
        return model;
    }

    private static String safeName(String name, String fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        return name;
    }
}
