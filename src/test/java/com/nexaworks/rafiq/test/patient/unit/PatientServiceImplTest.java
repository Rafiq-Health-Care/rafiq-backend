package com.nexaworks.rafiq.test.patient.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexaworks.rafiq.patient.api.dto.request.CompletePatientDataRequest;
import com.nexaworks.rafiq.patient.entity.enums.BloodType;
import com.nexaworks.rafiq.patient.entity.enums.SmokeStatus;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.repository.PatientRepository;
import com.nexaworks.rafiq.patient.service.WeightHistoryService;
import com.nexaworks.rafiq.patient.service.implementation.PatientServiceImpl;

@DisplayName("PatientService Test Cases")
class PatientServiceImplTest {
    @Mock
    PatientRepository patientRepository;

    @Mock
    WeightHistoryService weightHistoryService;

    @InjectMocks
    PatientServiceImpl patientService;

    private Patient patient;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        patient = Patient.builder().id(patientId).weight(70.0).build();
    }

    @Nested
    @DisplayName("Register Patient Tests")
    class RegisterPatientTests {

        @Test
        @DisplayName("Should register patient successfully")
        void register_ShouldRegisterPatientSuccessfully() {
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);

            patientService.register(patient);

            verify(patientRepository, times(1)).save(patient);
        }
    }

    @Nested
    @DisplayName("Complete Patient Profile Tests")
    class CompletePatientProfileTests {

        @Test
        @DisplayName("Should complete patient profile successfully when patient exists")
        void completePatientProfile_ShouldCompleteProfileSuccessfully_WhenPatientExists() {
            CompletePatientDataRequest request = new CompletePatientDataRequest(180, 75.0,
                    BloodType.A_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Jane Doe", "+10987654321");

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);
            doNothing().when(weightHistoryService).logNewWeight(any(), any(Patient.class));

            Patient result = patientService.completePatientProfile(request, patientId);

            assertThat(result).isNotNull();
            verify(patientRepository, times(1)).findById(patientId);
            verify(patientRepository, times(1)).save(patient);
        }

        @Test
        @DisplayName("Should log weight history when weight changes")
        void completePatientProfile_ShouldLogWeightHistory_WhenWeightChanges() {
            CompletePatientDataRequest request = new CompletePatientDataRequest(180, 80.0,
                    BloodType.A_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Jane Doe", "+10987654321");

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);
            doNothing().when(weightHistoryService).logNewWeight(any(), any(Patient.class));

            patientService.completePatientProfile(request, patientId);

            verify(weightHistoryService, times(1)).logNewWeight(80.0, patient);
        }

        @Test
        @DisplayName("Should not log weight history when weight unchanged")
        void completePatientProfile_ShouldNotLogWeightHistory_WhenWeightUnchanged() {
            CompletePatientDataRequest request = new CompletePatientDataRequest(180, 70.0,
                    BloodType.A_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Jane Doe", "+10987654321");

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);

            patientService.completePatientProfile(request, patientId);

            verify(weightHistoryService, never()).logNewWeight(any(), any(Patient.class));
        }

        @Test
        @DisplayName("Should throw exception when patient not found")
        void completePatientProfile_ShouldThrowException_WhenPatientNotFound() {
            CompletePatientDataRequest request = new CompletePatientDataRequest(180, 75.0,
                    BloodType.A_POSITIVE, SmokeStatus.NO, 0, null, false, 0, false, "Engineer",
                    "Jane Doe", "+10987654321");

            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.completePatientProfile(request, patientId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Patient not found with id: " + patientId);

            verify(patientRepository, never()).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should update all patient profile fields correctly")
        void completePatientProfile_ShouldUpdateAllFields_WhenRequestIsValid() {
            Date lastSmoked = new Date();
            CompletePatientDataRequest request = new CompletePatientDataRequest(185, 80.0,
                    BloodType.AB_NEGATIVE, SmokeStatus.YES, 10, lastSmoked, true, 5, false,
                    "Doctor", "Bob Smith", "+1122334455");

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(patientRepository.save(any(Patient.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(weightHistoryService).logNewWeight(any(), any(Patient.class));

            Patient result = patientService.completePatientProfile(request, patientId);

            assertThat(result.getHeight()).isEqualTo(185);
            assertThat(result.getWeight()).isEqualTo(80.0);
            assertThat(result.getBloodType()).isEqualTo(BloodType.AB_NEGATIVE);
            assertThat(result.getSmokeStatus()).isEqualTo(SmokeStatus.YES);
            assertThat(result.getCigarettesPerDay()).isEqualTo(10);
            assertThat(result.getLastSmoked()).isEqualTo(lastSmoked);
            assertThat(result.isAlcoholism()).isTrue();
            assertThat(result.getDrinksPerWeek()).isEqualTo(5);
            assertThat(result.getOccupation()).isEqualTo("Doctor");
            assertThat(result.getEmergencyContactName()).isEqualTo("Bob Smith");
            assertThat(result.getEmergencyContactPhone()).isEqualTo("+1122334455");
        }
    }
}
