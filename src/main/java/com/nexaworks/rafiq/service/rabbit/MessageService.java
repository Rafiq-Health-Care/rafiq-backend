package com.nexaworks.rafiq.service.rabbit;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.User;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MessageService {
    void sendResetPasswordEvent(User user, String token);
    void sendNewOtpEvent(User user, String otp);
    void sendRegistrationEvent(User user, String otp);

    void sendPatientCancelledEvent(Consultation consultation);

    void sendDoctorCancelledEvent(Consultation consultation);

    void publishExpirationEvent(UUID id, LocalDateTime endTime);

    void publishPreparationEvent(UUID id, LocalDateTime startTime);
}
