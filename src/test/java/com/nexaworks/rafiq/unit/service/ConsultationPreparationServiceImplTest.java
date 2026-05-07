package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexaworks.rafiq.dto.notificaiton.PushNotification;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.TimeSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.exception.custom.ConsultationException;
import com.nexaworks.rafiq.exception.custom.RtcProviderException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.service.call.RtcProvider;
import com.nexaworks.rafiq.service.consultation.ConsultationPreparationServiceImpl;
import com.nexaworks.rafiq.service.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultationPreparationServiceImpl Unit Tests")
class ConsultationPreparationServiceImplTest {

    @Mock
    private NotificationService<PushNotification> notificationService;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private RtcProvider rtcProvider;

    @InjectMocks
    private ConsultationPreparationServiceImpl preparationService;

    private UUID consultationId;
    private TimeSlot timeSlot;
    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        consultationId = UUID.randomUUID();
        LocalDateTime slotStart = LocalDateTime.now().plusMinutes(15);
        LocalDateTime slotEnd = LocalDateTime.now().plusHours(1);
        timeSlot = TimeSlot.builder().startTime(slotStart).endTime(slotEnd).durationMinutes(45)
                .build();

        doctor = new Doctor();
        doctor.setFirstName("Jane");
        doctor.setLastName("Doe");
        doctor.setNotificationToken("doctor-fcm-token");

        patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Smith");
        patient.setNotificationToken("patient-fcm-token");
    }

    @Test
    @DisplayName("should throw when consultation does not exist")
    void shouldThrowWhenConsultationNotFound() {
        when(consultationRepository.findConsultationById(consultationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preparationService.prepare(consultationId))
                .isInstanceOf(ConsultationException.class)
                .hasMessage("Consultation not found");

        verify(rtcProvider, never()).generateToken(any(), anyInt());
        verify(consultationRepository, never()).save(any());
        verify(notificationService, never()).sendNotification(any());
    }

    @Test
    @DisplayName("should exit when status is not preparable")
    void shouldDoNothingWhenStatusNotPreparable() {
        Consultation consultation = baseConsultation(ConsultationStatus.AVAILABLE);

        when(consultationRepository.findConsultationById(consultationId))
                .thenReturn(Optional.of(consultation));

        preparationService.prepare(consultationId);

        verify(rtcProvider, never()).generateToken(any(), anyInt());
        verify(consultationRepository, never()).save(any());
        verify(notificationService, never()).sendNotification(any());
    }

    @Test
    @DisplayName("should throw when RTC provider returns no token")
    void shouldThrowWhenRtcReturnsNull() {
        Consultation consultation = baseConsultation(ConsultationStatus.BOOKED);

        when(consultationRepository.findConsultationById(consultationId))
                .thenReturn(Optional.of(consultation));
        when(rtcProvider.generateToken(eq(consultationId.toString()), anyInt())).thenReturn(null);

        assertThatThrownBy(() -> preparationService.prepare(consultationId))
                .isInstanceOf(RtcProviderException.class)
                .hasMessage("Failed to generate access token for consultation");

        verify(consultationRepository, never()).save(any());
        verify(notificationService, never()).sendNotification(any());
    }

    @Test
    @DisplayName("should set LIVE, persist token, and notify patient and doctor when preparable")
    void shouldPrepareSuccessfully() {
        Consultation consultation = baseConsultation(ConsultationStatus.CONFIRMED);

        when(consultationRepository.findConsultationById(consultationId))
                .thenReturn(Optional.of(consultation));
        String token = "rtc-token-xyz";
        when(rtcProvider.generateToken(eq(consultationId.toString()), anyInt())).thenReturn(token);

        preparationService.prepare(consultationId);

        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.LIVE);
        assertThat(consultation.getAccessToken()).isEqualTo(token);
        verify(consultationRepository).save(consultation);
        verify(notificationService, times(2)).sendNotification(any(PushNotification.class));
    }

    private Consultation baseConsultation(ConsultationStatus status) {
        Consultation consultation = Consultation.builder().id(consultationId).status(status)
                .doctor(doctor).patient(patient).price(BigDecimal.TEN).build();
        consultation.setTimeSlot(timeSlot);
        return consultation;
    }
}
