package com.nexaworks.rafiq.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.MedicineRepository;
import com.nexaworks.rafiq.service.DrugService;
import com.nexaworks.rafiq.service.ServiceImpl.MedicineServiceImpl;
import com.nexaworks.rafiq.service.UserService;

@DisplayName("Medicine Service Unit test")
public class MedicineServiceImpTest {
    @Mock
    MedicineRepository medicineRepository;
    @Mock
    UserService userService;
    @Mock
    DrugService drugService;
    @InjectMocks
    MedicineServiceImpl medicineService;
    User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .patientProfile(PatientProfile.builder().medicines(new ArrayList<>()).build())
                .build();
    }
    @DisplayName("Add Medicine should add the medicine successfully when the patient doesn't have this medicine before and doesn't exceed the limit")
    @Test
    void addMedicine_ShouldAddMedicineSuccessfully_WhenPatientDoesNotHaveItBeforeAndDoesNotExceedTheLimit() {
        Medicine medicine = Medicine.builder().build();
        Drug drug = Drug.builder().id(java.util.UUID.randomUUID()).build();
        when(userService.getUser()).thenReturn(user);
        when(drugService.getDrugById(any())).thenReturn(drug);
        when(medicineRepository.save(medicine)).thenReturn(medicine);

        medicineService.addMedicine(medicine, UUID.randomUUID());

        verify(medicineRepository, times(1)).save(medicine);
    }
}
