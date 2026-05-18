package com.nexaworks.rafiq.service.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationLog;
import com.nexaworks.rafiq.repository.ConsultationLogRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultationLogService implements IConsultationLogService {
    private final AuthService authService;
    private final ConsultationLogRepository consultationLogRepository;

    @Override
    public void logEnter(Consultation consultation) {
        UUID userId = authService.getAuthenticateUserId();
        ConsultationLog consultationLog;
        if (consultation.getConsultationLog() == null) {
            consultationLog = new ConsultationLog();
            consultation.setConsultationLog(consultationLog);
        } else {
            consultationLog = consultation.getConsultationLog();
        }
        if (userId.equals(consultation.getPatient().getId())) {
            consultationLog.setPatientEnterTime(LocalDateTime.now());
        } else if (userId.equals(consultation.getDoctor().getId())) {
            consultationLog.setDoctorEnterTime(LocalDateTime.now());
        }
        consultationLogRepository.save(consultationLog);
    }

    @Override
    public void logLeave(Consultation consultation) {
        UUID userId = authService.getAuthenticateUserId();
        ConsultationLog consultationLog = consultation.getConsultationLog();
        if (userId.equals(consultation.getPatient().getId())) {
            consultationLog.setPatientLeaveTime(LocalDateTime.now());
        } else if (userId.equals(consultation.getDoctor().getId())) {
            consultationLog.setDoctorLeaveTime(LocalDateTime.now());
        }
    }
}
