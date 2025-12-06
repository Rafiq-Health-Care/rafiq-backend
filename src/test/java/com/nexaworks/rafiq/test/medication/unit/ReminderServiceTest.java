package com.nexaworks.rafiq.test.medication.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.model.Drug;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.medication.entity.model.Reminder;
import com.nexaworks.rafiq.medication.entity.model.ReminderLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.medication.api.dto.request.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.medication.api.dto.request.ReminderFilters;
import com.nexaworks.rafiq.medication.entity.enums.ReminderStatus;
import com.nexaworks.rafiq.medication.exception.ReminderNotFound;
import com.nexaworks.rafiq.medication.repository.ReminderLogRepository;
import com.nexaworks.rafiq.medication.repository.ReminderRepository;
import com.nexaworks.rafiq.medication.service.implementation.ReminderServiceImpl;
import com.nexaworks.rafiq.patient.service.PatientService;
import com.nexaworks.rafiq.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reminder Service Unit Tests")
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private ReminderLogRepository reminderLogRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReminderServiceImpl reminderService;

    private Medicine testMedicine;
    private Reminder testReminder;
    private UUID testPatientId;
    private UUID testReminderId;

    @BeforeEach
    void setUp() {
        testPatientId = UUID.randomUUID();
        testReminderId = UUID.randomUUID();

        testMedicine = Medicine.builder().id(UUID.randomUUID()).name("Test Medicine")
                .drug(Drug.builder().id(UUID.randomUUID()).build()).build();

        testReminder = Reminder.builder().id(testReminderId).medicine(testMedicine)
                .patientId(testPatientId).vibrate(true).status(ReminderStatus.UPCOMING)
                .nextReminder(LocalDateTime.now().plusHours(1)).disable(false).build();

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
        }
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Nested
    @DisplayName("Create Reminder Tests")
    class CreateReminderTests {

        @Test
        @DisplayName("Should create reminder successfully")
        void shouldCreateReminder_Successfully() {
            when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

            Reminder result = reminderService.createReminder(testReminder, testPatientId);

            assertThat(result).isNotNull();
            assertThat(result.getPatientId()).isEqualTo(testPatientId);
            verify(reminderRepository).save(testReminder);
        }

        @Test
        @DisplayName("Should set patient to reminder when creating")
        void shouldSetPatient_WhenCreatingReminder() {
            Reminder reminderWithoutPatient = Reminder.builder().medicine(testMedicine)
                    .vibrate(true).build();

            when(reminderRepository.save(any(Reminder.class))).thenReturn(reminderWithoutPatient);

            reminderService.createReminder(reminderWithoutPatient, testPatientId);

            ArgumentCaptor<Reminder> reminderCaptor = ArgumentCaptor.forClass(Reminder.class);
            verify(reminderRepository).save(reminderCaptor.capture());
            assertThat(reminderCaptor.getValue().getPatientId()).isEqualTo(testPatientId);
        }

        @Test
        @DisplayName("Should publish event after commit")
        void shouldPublishEvent_AfterCommit() {
            when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

            reminderService.createReminder(testReminder, testPatientId);

            verify(reminderRepository).save(testReminder);
        }

        @Test
        @DisplayName("Should save reminder with all properties")
        void shouldSaveReminder_WithAllProperties() {
            Reminder newReminder = Reminder.builder().medicine(testMedicine).vibrate(false)
                    .status(ReminderStatus.UPCOMING).nextReminder(LocalDateTime.now()).build();

            when(reminderRepository.save(any(Reminder.class))).thenReturn(newReminder);

            Reminder result = reminderService.createReminder(newReminder, testPatientId);

            assertThat(result.getMedicine()).isEqualTo(testMedicine);
            assertThat(result.isVibrate()).isFalse();
            assertThat(result.getStatus()).isEqualTo(ReminderStatus.UPCOMING);
            verify(reminderRepository).save(newReminder);
        }
    }

    @Nested
    @DisplayName("Get History Tests")
    class GetHistoryTests {

        @Test
        @DisplayName("Should get history with filters successfully")
        void shouldGetHistory_WithFilters() {
            Instant startDate = Instant.now().minusSeconds(7 * 24 * 60 * 60);
            Instant endDate = Instant.now();
            UUID medicineId = UUID.randomUUID();
            ReminderFilters filters = new ReminderFilters(startDate, endDate, medicineId,
                    ReminderStatus.TAKEN);
            Pageable pageable = PageRequest.of(0, 10);

            Page<GetAllRemindersHistoryResponseProjection> expectedPage = new PageImpl<>(List.of());
            when(reminderRepository.findReminderByMedicineId(filters.medicineId()))
                    .thenReturn(testReminderId);
            when(reminderLogRepository.findLogsHistory(eq(startDate), eq(endDate),
                    eq(testReminderId), eq(ReminderStatus.TAKEN), eq(testPatientId), eq(pageable)))
                    .thenReturn(expectedPage);

            Page<GetAllRemindersHistoryResponseProjection> result = reminderService
                    .getHistory(pageable, filters, testPatientId);

            assertThat(result).isNotNull();
            verify(reminderRepository).findReminderByMedicineId(filters.medicineId());
            verify(reminderLogRepository).findLogsHistory(startDate, endDate, testReminderId,
                    ReminderStatus.TAKEN, testPatientId, pageable);
        }

        @Test
        @DisplayName("Should get history without medicine filter")
        void shouldGetHistory_WithoutMedicineFilter() {
            ReminderFilters filters = new ReminderFilters(Instant.now().minusSeconds(24 * 60 * 60),
                    Instant.now(), null, ReminderStatus.MISSED);
            Pageable pageable = PageRequest.of(0, 20);

            Page<GetAllRemindersHistoryResponseProjection> expectedPage = new PageImpl<>(List.of());
            when(reminderLogRepository.findLogsHistory(any(), any(), eq(null),
                    eq(ReminderStatus.MISSED), eq(testPatientId), eq(pageable)))
                    .thenReturn(expectedPage);

            Page<GetAllRemindersHistoryResponseProjection> result = reminderService
                    .getHistory(pageable, filters, testPatientId);

            assertThat(result).isNotNull();
            verify(reminderRepository, never()).findReminderByMedicineId(any());
        }

        @Test
        @DisplayName("Should get history with custom page size")
        void shouldGetHistory_WithCustomPageSize() {
            ReminderFilters filters = new ReminderFilters(null, null, null, null);
            Pageable pageable = PageRequest.of(2, 50);

            Page<GetAllRemindersHistoryResponseProjection> expectedPage = new PageImpl<>(List.of());
            when(reminderLogRepository.findLogsHistory(any(), any(), eq(null), eq(null),
                    eq(testPatientId), eq(pageable))).thenReturn(expectedPage);

            Page<GetAllRemindersHistoryResponseProjection> result = reminderService
                    .getHistory(pageable, filters, testPatientId);

            assertThat(result).isNotNull();
            verify(reminderLogRepository).findLogsHistory(null, null, null, null, testPatientId,
                    pageable);
        }

        @Test
        @DisplayName("Should get history for specific patient only")
        void shouldGetHistory_ForSpecificPatientOnly() {
            ReminderFilters filters = new ReminderFilters(null, null, null, null);
            Pageable pageable = PageRequest.of(0, 10);

            UUID differentPatientId = UUID.randomUUID();

            when(reminderLogRepository.findLogsHistory(any(), any(), any(), any(),
                    eq(differentPatientId), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

            reminderService.getHistory(pageable, filters, differentPatientId);

            verify(reminderLogRepository).findLogsHistory(null, null, null, null,
                    differentPatientId, pageable);
        }
    }

    @Nested
    @DisplayName("Update Reminder Status Tests")
    class UpdateReminderStatusTests {

        @Test
        @DisplayName("Should update reminder status to TAKEN successfully")
        void shouldUpdateReminderStatus_ToTaken() {
            LocalDateTime takenTime = LocalDateTime.now();
            when(reminderRepository.findById(testReminderId)).thenReturn(Optional.of(testReminder));
            when(reminderLogRepository.save(any(ReminderLog.class))).thenReturn(new ReminderLog());

            reminderService.updateReminderStatus(testReminderId, ReminderStatus.TAKEN, takenTime,
                    testPatientId);

            ArgumentCaptor<ReminderLog> logCaptor = ArgumentCaptor.forClass(ReminderLog.class);
            verify(reminderLogRepository).save(logCaptor.capture());

            ReminderLog savedLog = logCaptor.getValue();
            assertThat(savedLog.getStatus()).isEqualTo(ReminderStatus.TAKEN);
            assertThat(savedLog.getReminder()).isEqualTo(testReminder);
            assertThat(savedLog.getPatientId()).isEqualTo(testPatientId);
            assertThat(savedLog.getTimestamp()).isEqualTo(takenTime);
        }

        @Test
        @DisplayName("Should update reminder status to MISSED successfully")
        void shouldUpdateReminderStatus_ToMissed() {
            LocalDateTime missedTime = LocalDateTime.now().minusHours(2);
            when(reminderRepository.findById(testReminderId)).thenReturn(Optional.of(testReminder));
            when(reminderLogRepository.save(any(ReminderLog.class))).thenReturn(new ReminderLog());

            reminderService.updateReminderStatus(testReminderId, ReminderStatus.MISSED, missedTime,
                    testPatientId);

            ArgumentCaptor<ReminderLog> logCaptor = ArgumentCaptor.forClass(ReminderLog.class);
            verify(reminderLogRepository).save(logCaptor.capture());

            ReminderLog savedLog = logCaptor.getValue();
            assertThat(savedLog.getStatus()).isEqualTo(ReminderStatus.MISSED);
            assertThat(savedLog.getTimestamp()).isEqualTo(missedTime);
        }

        @Test
        @DisplayName("Should throw exception when reminder not found")
        void shouldThrowException_WhenReminderNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            when(reminderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reminderService.updateReminderStatus(nonExistentId,
                    ReminderStatus.TAKEN, LocalDateTime.now(), testPatientId))
                    .isInstanceOf(ReminderNotFound.class)
                    .hasMessageContaining("Reminder not found");

            verify(reminderLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when patient doesn't own reminder")
        void shouldThrowException_WhenPatientDoesntOwnReminder() {
            UUID differentPatientId = UUID.randomUUID();

            when(reminderRepository.findById(testReminderId)).thenReturn(Optional.of(testReminder));

            assertThatThrownBy(() -> reminderService.updateReminderStatus(testReminderId,
                    ReminderStatus.TAKEN, LocalDateTime.now(), differentPatientId))
                    .isInstanceOf(ReminderNotFound.class)
                    .hasMessageContaining("Invalid Reminder Id");

            verify(reminderLogRepository, never()).save(any());
        }
    }
}

