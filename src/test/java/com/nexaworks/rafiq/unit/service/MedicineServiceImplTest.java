package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexaworks.rafiq.dto.request.medicine.BulkMedicineOperationRequest;
import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.Action;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.exception.custom.GroupNotFoundException;
import com.nexaworks.rafiq.exception.custom.MedicineAlreadyExist;
import com.nexaworks.rafiq.exception.custom.MedicineLimit;
import com.nexaworks.rafiq.repository.MedicineRepository;
import com.nexaworks.rafiq.secheduler.service.QuartzSchedulerService;
import com.nexaworks.rafiq.service.medicine.implementation.DrugServiceImpl;
import com.nexaworks.rafiq.service.medicine.implementation.GroupServiceImpl;
import com.nexaworks.rafiq.service.medicine.implementation.MedicineServiceImpl;
import com.nexaworks.rafiq.service.patient.PatientService;
import com.nexaworks.rafiq.service.user.UserService;

import jakarta.validation.ValidationException;

@DisplayName("MedicineService Test Cases")
public class MedicineServiceImplTest {
    @Mock
    MedicineRepository medicineRepository;
    @Mock
    DrugServiceImpl drugService;
    @Mock
    PatientService patientService;
    @Mock
    GroupServiceImpl groupService;
    @Mock
    QuartzSchedulerService quartzSchedulerService;
    @Mock
    UserService userService;
    @InjectMocks
    MedicineServiceImpl medicineService;
    static Patient patient;
    UUID drugId = UUID.randomUUID();
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = Patient.builder().id(UUID.randomUUID()).build();
    }
    @Nested
    @DisplayName("Add medicine test")
    class AddMedicineTest {
        @DisplayName("Add medicine should add medicine successfully when the user doesn't have this medicine before and doesn't exceed the limit")
        @Test
        void addMedicine_ShouldAddMedicineSuccessfully_WhenUserDoesntHaveThisMedicineBeforeAndDoesntExceedTheLimit() {
            Medicine entity = Medicine.builder().dosage("100 mlg").build();
            Drug drug = Drug.builder().id(drugId).tradeName("Advil").build();
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(medicineRepository.existsByPatientIdAndDrugId(any(), any())).thenReturn(false);
            when(medicineRepository.countByPatientId(any())).thenReturn(0);
            when(medicineRepository.save(any())).thenReturn(entity);
            when(drugService.getDrugById(any())).thenReturn(drug);

            Medicine medicine = medicineService.addMedicine(entity, drugId);

            assertThat(medicine.getDrug().getId()).isEqualTo(drug.getId());
            assertThat(medicine.getPatient()).isEqualTo(patient);
            assertThat(medicine.getName()).isEqualTo("Advil");
        }
        @Test
        @DisplayName("Add medicine should throw MedicineAlreadyExist when the user already has this medicine")
        void addMedicine_ShouldThrowMedicineAlreadyExist_WhenUserAlreadyHasThisMedicine() {
            Medicine entity = Medicine.builder().dosage("100 mlg").build();
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(medicineRepository.existsByPatientIdAndDrugId(any(), any())).thenReturn(true);

            assertThatExceptionOfType(MedicineAlreadyExist.class)
                    .isThrownBy(() -> medicineService.addMedicine(entity, drugId))
                    .withMessage("Medicine already exist");
        }
        @Test
        @DisplayName("Add medicine should throw MedicineLimit when the user exceed the medicine limit")
        void addMedicine_ShouldThrowMedicineLimit_WhenUserExceedTheMedicineLimit() {
            Medicine entity = Medicine.builder().dosage("100 mlg").build();
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(medicineRepository.countByPatientId(any())).thenReturn(200);

            assertThatExceptionOfType(MedicineLimit.class)
                    .isThrownBy(() -> medicineService.addMedicine(entity, drugId))
                    .withMessage("You have reached the limit of medicine");
        }

    }

    @Nested
    @DisplayName("Bulk Operations Test")
    class BulkOperationsTest {

        @Nested
        @DisplayName("Delete Operation")
        class DeleteOperationTest {
            @Test
            @DisplayName("Should delete all medicines successfully when all IDs are valid")
            void bulkDelete_ShouldDeleteAllMedicinesSuccessfully_WhenAllIdsAreValid()
                    throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.DELETE, Optional.empty());

                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patient(patient).build();
                when(userService.getUserId()).thenReturn(patient.getId());

                when(patientService.getPatientProfile()).thenReturn(patient);
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                doNothing().when(medicineRepository).delete(any(Medicine.class));

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(0);
                verify(medicineRepository, times(2)).delete(any(Medicine.class));
            }

            @Test
            @DisplayName("Should return failed IDs when some medicines are not found")
            void bulkDelete_ShouldReturnFailedIds_WhenSomeMedicinesAreNotFound() throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                UUID medicineId3 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2, medicineId3);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.DELETE, Optional.empty());

                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient).build();
                when(userService.getUserId()).thenReturn(patient.getId());

                when(patientService.getPatientProfile()).thenReturn(patient);
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.empty());
                when(medicineRepository.findById(medicineId3)).thenReturn(Optional.empty());
                doNothing().when(medicineRepository).delete(any(Medicine.class));

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(2);
                assertThat(failedIds.contains(medicineId2)).isTrue();
                assertThat(failedIds.contains(medicineId3)).isTrue();
                verify(medicineRepository, times(1)).delete(any(Medicine.class));
            }
            @Test
            @DisplayName("Should handle empty selection - no medicines selected")
            void bulkDelete_ShouldHandleEmptySelection_WhenNoMedicinesSelected() {
                List<UUID> medicineIds = List.of();
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.DELETE, Optional.empty());

                when(patientService.getPatientProfile()).thenReturn(patient);

                assertThatExceptionOfType(ValidationException.class)
                        .isThrownBy(() -> medicineService.bulkMedicineOperation(request))
                        .withMessage("Medicine ids cannot be empty");

            }

            @Test
            @DisplayName("Should only delete medicines belonging to current patient")
            void bulkDelete_ShouldOnlyDeleteOwnMedicines_WhenMedicinesBelongToDifferentPatients()
                    throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                UUID medicineId3 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2, medicineId3);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.DELETE, Optional.empty());

                Patient otherPatient = Patient.builder().id(UUID.randomUUID()).build();

                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patient(otherPatient)
                        .build();
                Medicine medicine3 = Medicine.builder().id(medicineId3).patient(patient).build();

                when(patientService.getPatientProfile()).thenReturn(patient);
                when(userService.getUserId()).thenReturn(patient.getId());
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                when(medicineRepository.findById(medicineId3)).thenReturn(Optional.of(medicine3));
                doNothing().when(medicineRepository).delete(any(Medicine.class));

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(1);
                assertThat(failedIds.contains(medicineId2)).isTrue();
                verify(medicineRepository, times(2)).delete(any(Medicine.class));
            }
            @Test
            @DisplayName("Should delete  medicines when belong to current patient")
            void bulkDelete_ShouldDeleteMedicines_WhenBelongToCurrentPatient() throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.DELETE, Optional.empty());
                Medicine medicine1 = Medicine.builder().id(medicineId1)
                        .patient(Patient.builder().id(UUID.randomUUID()).build()).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patient(patient).build();
                when(userService.getUserId()).thenReturn(patient.getId());

                when(patientService.getPatientProfile()).thenReturn(patient);
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                doNothing().when(medicineRepository).delete(any(Medicine.class));

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(1);
                verify(medicineRepository, times(1)).delete(any(Medicine.class));

            }

        }

        @Nested
        @DisplayName("Move to Group Operation")
        class MoveToGroupOperationTest {
            @Test
            @DisplayName("Should move all medicines to group successfully when group ID is provided and all IDs are valid")
            void bulkMoveToGroup_ShouldMoveAllMedicinesToGroup_WhenGroupIdProvidedAndAllIdsValid()
                    throws Exception {
                UUID groupId = UUID.randomUUID();
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.MOVE_TO_GROUP, Optional.of(groupId));

                Group group = Group.builder().id(groupId).patient(patient).build();
                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patient(patient).build();

                when(groupService.getGroupById(groupId)).thenReturn(group);
                when(userService.getUserId()).thenReturn(patient.getId());
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(0);
                verify(medicineRepository, times(2)).save(any(Medicine.class));
                assertThat(medicine1.getGroup()).isEqualTo(group);
                assertThat(medicine2.getGroup()).isEqualTo(group);
            }

            @Test
            @DisplayName("Should throw exception when group ID is not provided")
            void bulkMoveToGroup_ShouldThrowException_WhenGroupIdNotProvided() {
                UUID medicineId1 = UUID.randomUUID();
                List<UUID> medicineIds = List.of(medicineId1);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.MOVE_TO_GROUP, Optional.empty());

                assertThatExceptionOfType(InvocationTargetException.class)
                        .isThrownBy(() -> medicineService.bulkMedicineOperation(request))
                        .withCauseInstanceOf(GroupNotFoundException.class);
            }

            @Test
            @DisplayName("Should return failed IDs when some medicines are not found")
            void bulkMoveToGroup_ShouldReturnFailedIds_WhenSomeMedicinesNotFound()
                    throws Exception {
                UUID groupId = UUID.randomUUID();
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.MOVE_TO_GROUP, Optional.of(groupId));

                Group group = Group.builder().id(groupId).patient(patient).build();
                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient).build();

                when(groupService.getGroupById(groupId)).thenReturn(group);
                when(userService.getUserId()).thenReturn(patient.getId());
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.empty());
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(1);
                assertThat(failedIds.contains(medicineId2)).isTrue();
                verify(medicineRepository, times(1)).save(any(Medicine.class));
            }
        }

        @Nested
        @DisplayName("Mark Active Operation")
        class MarkActiveOperationTest {
            @Test
            @DisplayName("Should mark all medicines as active successfully when all IDs are valid")
            void bulkMarkActive_ShouldMarkAllMedicinesAsActive_WhenAllIdsAreValid()
                    throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.MARK_ACTIVE, Optional.empty());

                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient)
                        .status(MedicineStatus.INACTIVE).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patient(patient)
                        .status(MedicineStatus.INACTIVE).build();

                when(userService.getUserId()).thenReturn(patient.getId());
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(0);
                verify(medicineRepository, times(2)).save(any(Medicine.class));
                assertThat(medicine1.getStatus()).isEqualTo(MedicineStatus.ACTIVE);
                assertThat(medicine2.getStatus()).isEqualTo(MedicineStatus.ACTIVE);
            }

            @Test
            @DisplayName("Should return failed IDs when some medicines are not found")
            void bulkMarkActive_ShouldReturnFailedIds_WhenSomeMedicinesNotFound() throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.MARK_ACTIVE, Optional.empty());

                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient)
                        .status(MedicineStatus.INACTIVE).build();

                when(userService.getUserId()).thenReturn(patient.getId());
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.empty());
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(1);
                assertThat(failedIds.contains(medicineId2)).isTrue();
                verify(medicineRepository, times(1)).save(any(Medicine.class));
            }
        }

        @Nested
        @DisplayName("Mark Inactive Operation")
        class MarkInactiveOperationTest {
            @Test
            @DisplayName("Should mark all medicines as inactive successfully when all IDs are valid")
            void bulkMarkInactive_ShouldMarkAllMedicinesAsInactive_WhenAllIdsAreValid()
                    throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.MARK_INACTIVE, Optional.empty());

                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient)
                        .status(MedicineStatus.ACTIVE).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patient(patient)
                        .status(MedicineStatus.ACTIVE).build();

                when(patientService.getPatientProfile()).thenReturn(patient);
                when(userService.getUserId()).thenReturn(patient.getId());
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(0);
                verify(medicineRepository, times(2)).save(any(Medicine.class));
                assertThat(medicine1.getStatus()).isEqualTo(MedicineStatus.INACTIVE);
                assertThat(medicine2.getStatus()).isEqualTo(MedicineStatus.INACTIVE);
            }

            @Test
            @DisplayName("Should return failed IDs when some medicines are not found")
            void bulkMarkInactive_ShouldReturnFailedIds_WhenSomeMedicinesNotFound()
                    throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.MARK_INACTIVE, Optional.empty());

                Medicine medicine1 = Medicine.builder().id(medicineId1).patient(patient)
                        .status(MedicineStatus.ACTIVE).build();

                when(patientService.getPatientProfile()).thenReturn(patient);
                when(userService.getUserId()).thenReturn(patient.getId());
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.empty());
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request);

                assertThat(failedIds.size()).isEqualTo(1);
                assertThat(failedIds.contains(medicineId2)).isTrue();
                verify(medicineRepository, times(1)).save(any(Medicine.class));
            }
        }
    }
}
