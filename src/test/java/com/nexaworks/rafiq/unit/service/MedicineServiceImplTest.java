package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.exception.custom.MedicineAlreadyExist;
import com.nexaworks.rafiq.exception.custom.MedicineLimit;
import com.nexaworks.rafiq.repository.MedicineRepository;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.service.ServiceImpl.DrugServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.GroupServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.MedicineServiceImpl;

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
    @InjectMocks
    MedicineServiceImpl medicineService;
    static PatientProfile patient;
    UUID drugId = UUID.randomUUID();
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = PatientProfile.builder().id(UUID.randomUUID()).build();
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
}
