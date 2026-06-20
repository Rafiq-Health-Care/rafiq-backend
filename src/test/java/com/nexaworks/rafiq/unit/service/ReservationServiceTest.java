package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.nexaworks.rafiq.dto.request.consultation.ReserveConsultationRequest;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.entities.enums.SlotStatus;
import com.nexaworks.rafiq.exception.custom.consultation.ConsultationOverlappingException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotNotFoundException;
import com.nexaworks.rafiq.exception.custom.consultation.SlotReservedException;
import com.nexaworks.rafiq.repository.ConsultationRepository;
import com.nexaworks.rafiq.repository.ConsultationSlotRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.consultation.ReservationService;
import com.nexaworks.rafiq.service.payment.PaymentService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationServiceImpl Unit Tests")
class ReservationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private ConsultationSlotRepository slotRepository;

    @Mock
    private AuthService authService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private ReservationService reservationService;

    private Patient patient;
    private UUID slotId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private ConsultationSlot slot;

    @BeforeEach
    void setUp() {
        slotId = UUID.randomUUID();
        patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setEmail("patient@test.com");

        slotStart = LocalDateTime.of(2026, 5, 1, 10, 0);
        slotEnd = LocalDateTime.of(2026, 5, 1, 11, 0);
        slot = ConsultationSlot.builder().id(slotId).startTime(slotStart).endTime(slotEnd)
                .durationMinutes(60).status(SlotStatus.AVAILABLE).build();
    }

    @Test
    @DisplayName("should reserve consultation successfully and return client secret")
    void shouldReserveSuccessfullyAndReturnClientSecret() throws Exception {
        ReserveConsultationRequest request = new ReserveConsultationRequest(slotId, "notes",
                PaymentProvider.STRIPE);

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(slotRepository.findConsultationByIdWithLock(slotId)).thenReturn(Optional.of(slot));
        when(consultationRepository.existsByPatientOverlapping(eq(slotStart), eq(slotEnd),
                eq(patient.getId()))).thenReturn(false);
        when(paymentService.process(any(Consultation.class), eq(patient),
                eq(PaymentProvider.STRIPE))).thenReturn("pi_secret_xyz");

        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(
                TransactionSynchronizationManager.class)) {
            tx.when(() -> TransactionSynchronizationManager
                    .registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> null);

            String secret = reservationService.reserve(request);

            assertThat(secret).isEqualTo("pi_secret_xyz");
        }

        assertThat(slot.getStatus()).isEqualTo(SlotStatus.PENDING_PAYMENT);
        verify(consultationRepository).save(any(Consultation.class));
        verify(paymentService).process(any(Consultation.class), eq(patient),
                eq(PaymentProvider.STRIPE));
    }

    @Test
    @DisplayName("should throw when slot status is not AVAILABLE")
    void shouldThrowWhenConsultationStatusIsNotAvailable() throws Exception {
        ReserveConsultationRequest request = new ReserveConsultationRequest(slotId, null,
                PaymentProvider.STRIPE);
        slot.setStatus(SlotStatus.BOOKED);

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(slotRepository.findConsultationByIdWithLock(slotId)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> reservationService.reserve(request))
                .isInstanceOf(SlotReservedException.class)
                .hasMessage("Consultation cannot be reserved");

        verify(consultationRepository, never()).existsByPatientOverlapping(any(), any(), any());
        verify(paymentService, never()).process(any(), any(), any());
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when patient has overlapping consultation")
    void shouldThrowWhenPatientHasOverlappingConsultation() throws Exception {
        ReserveConsultationRequest request = new ReserveConsultationRequest(slotId, null,
                PaymentProvider.STRIPE);

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(slotRepository.findConsultationByIdWithLock(slotId)).thenReturn(Optional.of(slot));
        when(consultationRepository.existsByPatientOverlapping(eq(slotStart), eq(slotEnd),
                eq(patient.getId()))).thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserve(request))
                .isInstanceOf(ConsultationOverlappingException.class)
                .hasMessage("Consultation time slot is already booked");

        verify(paymentService, never()).process(any(), any(), any());
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should not update consultation status if payment service fails")
    void shouldNotPersistConsultationWhenPaymentFails() throws Exception {
        ReserveConsultationRequest request = new ReserveConsultationRequest(slotId, null,
                PaymentProvider.STRIPE);

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(slotRepository.findConsultationByIdWithLock(slotId)).thenReturn(Optional.of(slot));
        when(consultationRepository.existsByPatientOverlapping(eq(slotStart), eq(slotEnd),
                eq(patient.getId()))).thenReturn(false);
        when(paymentService.process(any(Consultation.class), eq(patient),
                eq(PaymentProvider.STRIPE)))
                .thenThrow(new RuntimeException("payment gateway error"));

        assertThatThrownBy(() -> reservationService.reserve(request))
                .isInstanceOf(RuntimeException.class).hasMessage("payment gateway error");

        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when slot is not found")
    void shouldThrowWhenSlotNotFound() {
        ReserveConsultationRequest request = new ReserveConsultationRequest(slotId, null,
                PaymentProvider.STRIPE);

        when(authService.getAuthenticateUser()).thenReturn(patient);
        when(slotRepository.findConsultationByIdWithLock(slotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserve(request))
                .isInstanceOf(SlotNotFoundException.class).hasMessage("Consultation not found");
    }
}
