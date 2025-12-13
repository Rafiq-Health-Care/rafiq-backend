package com.nexaworks.rafiq.test.medication.unit;

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

import com.nexaworks.rafiq.medication.api.dto.request.BulkMedicineOperationRequest;
import com.nexaworks.rafiq.medication.entity.model.Drug;
import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.medication.entity.enums.Action;
import com.nexaworks.rafiq.medication.entity.enums.MedicineStatus;
import com.nexaworks.rafiq.medication.exception.GroupNotFoundException;
import com.nexaworks.rafiq.medication.exception.MedicineAlreadyExist;
import com.nexaworks.rafiq.medication.exception.MedicineLimit;
import com.nexaworks.rafiq.medication.repository.MedicineRepository;
import com.nexaworks.rafiq.medication.service.implementation.DrugServiceImpl;
import com.nexaworks.rafiq.medication.service.implementation.GroupServiceImpl;
import com.nexaworks.rafiq.medication.service.implementation.MedicineServiceImpl;
import com.nexaworks.rafiq.patient.service.PatientService;
import com.nexaworks.rafiq.user.service.UserService;

import jakarta.validation.ValidationException;

@DisplayName("MedicineService Test Cases")
class MedicineServiceImplTest {
    @Mock
    MedicineRepository medicineRepository;
    @Mock
    DrugServiceImpl drugService;
    @Mock
    PatientService patientService;
    @Mock
    GroupServiceImpl groupService;
    @Mock
    UserService userService;
    @InjectMocks
    MedicineServiceImpl medicineService;
    static Patient patient;
    static UUID patientId;
    UUID drugId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = Patient.builder().id(UUID.randomUUID()).build();
        patientId = patient.getId();
    }

    @Nested
    @DisplayName("Add medicine test")
    class AddMedicineTest {
        @DisplayName("Add medicine should add medicine successfully when the user doesn't have this medicine before and doesn't exceed the limit")
        @Test
        void addMedicine_ShouldAddMedicineSuccessfully_WhenUserDoesntHaveThisMedicineBeforeAndDoesntExceedTheLimit() {
            Medicine entity = Medicine.builder().dosage("100 mlg").build();
            Drug drug = Drug.builder().id(drugId).tradeName("Advil").build();
            when(medicineRepository.existsByPatientIdAndDrugId(patientId, drugId)).thenReturn(false);
            when(medicineRepository.countByPatientId(patientId)).thenReturn(0);
            when(medicineRepository.save(any())).thenReturn(entity);
            when(drugService.getDrugById(drugId)).thenReturn(drug);

            Medicine medicine = medicineService.addMedicine(entity, drugId, patientId);

            assertThat(medicine.getDrug().getId()).isEqualTo(drug.getId());
            assertThat(medicine.getName()).isEqualTo("Advil");
        }

        @Test
        @DisplayName("Add medicine should throw MedicineAlreadyExist when the user already has this medicine")
        void addMedicine_ShouldThrowMedicineAlreadyExist_WhenUserAlreadyHasThisMedicine() {
            Medicine entity = Medicine.builder().dosage("100 mlg").build();
            when(medicineRepository.existsByPatientIdAndDrugId(patientId, drugId)).thenReturn(true);

            assertThatExceptionOfType(MedicineAlreadyExist.class)
                    .isThrownBy(() -> medicineService.addMedicine(entity, drugId, patientId))
                    .withMessage("Medicine already exist");
        }

        @Test
        @DisplayName("Add medicine should throw MedicineLimit when the user exceed the medicine limit")
        void addMedicine_ShouldThrowMedicineLimit_WhenUserExceedTheMedicineLimit() {
            Medicine entity = Medicine.builder().dosage("100 mlg").build();
            when(medicineRepository.countByPatientId(patientId)).thenReturn(200);

            assertThatExceptionOfType(MedicineLimit.class)
                    .isThrownBy(() -> medicineService.addMedicine(entity, drugId, patientId))
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

                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patientId(patientId).build();

                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                doNothing().when(medicineRepository).delete(any(Medicine.class));

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

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

                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId).build();

                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.empty());
                when(medicineRepository.findById(medicineId3)).thenReturn(Optional.empty());
                doNothing().when(medicineRepository).delete(any(Medicine.class));

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

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

                assertThatExceptionOfType(ValidationException.class)
                        .isThrownBy(() -> medicineService.bulkMedicineOperation(request, patientId))
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

                UUID otherPatientId = UUID.randomUUID();

                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patientId(otherPatientId)
                        .build();
                Medicine medicine3 = Medicine.builder().id(medicineId3).patientId(patientId).build();

                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                when(medicineRepository.findById(medicineId3)).thenReturn(Optional.of(medicine3));
                doNothing().when(medicineRepository).delete(any(Medicine.class));

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

                assertThat(failedIds.size()).isEqualTo(1);
                assertThat(failedIds.contains(medicineId2)).isTrue();
                verify(medicineRepository, times(2)).delete(any(Medicine.class));
            }

            @Test
            @DisplayName("Should delete medicines when belong to current patient")
            void bulkDelete_ShouldDeleteMedicines_WhenBelongToCurrentPatient() throws Exception {
                UUID medicineId1 = UUID.randomUUID();
                UUID medicineId2 = UUID.randomUUID();
                List<UUID> medicineIds = Arrays.asList(medicineId1, medicineId2);
                BulkMedicineOperationRequest request = new BulkMedicineOperationRequest(medicineIds,
                        Action.DELETE, Optional.empty());
                Medicine medicine1 = Medicine.builder().id(medicineId1)
                        .patientId(UUID.randomUUID()).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patientId(patientId).build();

                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                doNothing().when(medicineRepository).delete(any(Medicine.class));

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

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

                Group group = Group.builder().id(groupId).patientId(patientId).build();
                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patientId(patientId).build();

                when(groupService.getGroupById(groupId, patientId)).thenReturn(group);
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

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
                        .isThrownBy(() -> medicineService.bulkMedicineOperation(request, patientId))
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

                Group group = Group.builder().id(groupId).patientId(patientId).build();
                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId).build();

                when(groupService.getGroupById(groupId, patientId)).thenReturn(group);
                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.empty());
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

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

                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId)
                        .status(MedicineStatus.INACTIVE).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patientId(patientId)
                        .status(MedicineStatus.INACTIVE).build();

                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

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

                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId)
                        .status(MedicineStatus.INACTIVE).build();

                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.empty());
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

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

                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId)
                        .status(MedicineStatus.ACTIVE).build();
                Medicine medicine2 = Medicine.builder().id(medicineId2).patientId(patientId)
                        .status(MedicineStatus.ACTIVE).build();

                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.of(medicine2));
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

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

                Medicine medicine1 = Medicine.builder().id(medicineId1).patientId(patientId)
                        .status(MedicineStatus.ACTIVE).build();

                when(medicineRepository.findById(medicineId1)).thenReturn(Optional.of(medicine1));
                when(medicineRepository.findById(medicineId2)).thenReturn(Optional.empty());
                when(medicineRepository.save(any(Medicine.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

                List<UUID> failedIds = medicineService.bulkMedicineOperation(request, patientId);

                assertThat(failedIds.size()).isEqualTo(1);
                assertThat(failedIds.contains(medicineId2)).isTrue();
                verify(medicineRepository, times(1)).save(any(Medicine.class));
            }
        }
    }
}

