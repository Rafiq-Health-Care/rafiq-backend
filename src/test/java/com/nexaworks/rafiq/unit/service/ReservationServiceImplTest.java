package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.TimeSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.exception.custom.ConsultationException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.consultation.ReservationServiceImpl;
import com.nexaworks.rafiq.service.payment.PaymentService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationServiceImpl Unit Tests")
class ReservationServiceImplTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private AuthService authService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Patient patient;
    private UUID consultationId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private TimeSlot timeSlot;

    @BeforeEach
    void setUp() {
        consultationId = UUID.randomUUID();
        patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setEmail("patient@test.com");

        slotStart = LocalDateTime.of(2026, 5, 1, 10, 0);
        slotEnd = LocalDateTime.of(2026, 5, 1, 11, 0);
        timeSlot = TimeSlot.builder()
                .startTime(slotStart)
                .endTime(slotEnd)
                .durationMinutes(60)
                .build();
    }

    @Test
    @DisplayName("should reserve consultation successfully and return client secret")
    void shouldReserveSuccessfullyAndReturnClientSecret() {
        Consultation consultation = Consultation.builder()
                .id(consultationId)
                .status(ConsultationStatus.AVAILABLE)
                .timeSlot(timeSlot)
                .price(BigDecimal.valueOf(50))
                .build();

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(consultationRepository.findConsultationById(consultationId)).thenReturn(Optional.of(consultation));
        when(consultationRepository.existsByPatientOverlapping(
                eq(slotStart), eq(slotEnd), eq(patient.getId()), eq(ConsultationStatus.AVAILABLE))).thenReturn(false);
        when(paymentService.process(consultation, patient, PaymentProvider.STRIPE)).thenReturn("pi_secret_xyz");

        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(TransactionSynchronizationManager.class)) {
            tx.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> null);

            String secret = reservationService.reserve(consultationId, PaymentProvider.STRIPE);

            assertThat(secret).isEqualTo("pi_secret_xyz");
        }

        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.BOOKED);
        assertThat(consultation.getPatient()).isEqualTo(patient);
        verify(consultationRepository).save(consultation);
        verify(paymentService).process(consultation, patient, PaymentProvider.STRIPE);
    }

    @Test
    @DisplayName("should throw when consultation status is not AVAILABLE")
    void shouldThrowWhenConsultationStatusIsNotAvailable() {
        Consultation consultation = Consultation.builder()
                .id(consultationId)
                .status(ConsultationStatus.BOOKED)
                .timeSlot(timeSlot)
                .price(BigDecimal.TEN)
                .build();

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(consultationRepository.findConsultationById(consultationId)).thenReturn(Optional.of(consultation));

        assertThatThrownBy(() -> reservationService.reserve(consultationId, PaymentProvider.STRIPE))
                .isInstanceOf(ConsultationException.class)
                .hasMessage("Consultation cannot be reserved");

        verify(consultationRepository, never()).existsByPatientOverlapping(any(), any(), any(), any());
        verify(paymentService, never()).process(any(), any(), any());
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when patient has overlapping consultation")
    void shouldThrowWhenPatientHasOverlappingConsultation() {
        Consultation consultation = Consultation.builder()
                .id(consultationId)
                .status(ConsultationStatus.AVAILABLE)
                .timeSlot(timeSlot)
                .price(BigDecimal.TEN)
                .build();

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(consultationRepository.findConsultationById(consultationId)).thenReturn(Optional.of(consultation));
        when(consultationRepository.existsByPatientOverlapping(
                eq(slotStart), eq(slotEnd), eq(patient.getId()), eq(ConsultationStatus.AVAILABLE))).thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserve(consultationId, PaymentProvider.STRIPE))
                .isInstanceOf(ConsultationException.class)
                .hasMessage("Consultation time slot is already booked");

        verify(paymentService, never()).process(any(), any(), any());
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should not update consultation status if payment service fails")
    void shouldNotPersistConsultationWhenPaymentFails() {
        Consultation consultation = Consultation.builder()
                .id(consultationId)
                .status(ConsultationStatus.AVAILABLE)
                .timeSlot(timeSlot)
                .price(BigDecimal.TEN)
                .build();

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(consultationRepository.findConsultationById(consultationId)).thenReturn(Optional.of(consultation));
        when(consultationRepository.existsByPatientOverlapping(
                eq(slotStart), eq(slotEnd), eq(patient.getId()), eq(ConsultationStatus.AVAILABLE))).thenReturn(false);
        when(paymentService.process(consultation, patient, PaymentProvider.STRIPE))
                .thenThrow(new RuntimeException("payment gateway error"));

        assertThatThrownBy(() -> reservationService.reserve(consultationId, PaymentProvider.STRIPE))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("payment gateway error");

        verify(consultationRepository, never()).save(any());
    }
}
