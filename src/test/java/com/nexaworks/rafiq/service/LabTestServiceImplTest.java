package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.LabTest;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.LabTestRepository;
import com.nexaworks.rafiq.service.ServiceImpl.LabResultServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.LabTestServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.PatientServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class LabTestServiceImplTest {
    @Mock
    LabResultServiceImpl labResultService;
    @Mock
    LabTestRepository labTestRepository;
    @Mock
    ImageService imageService;
    @Mock
    PatientServiceImpl patientService;
    @Mock
    UserServiceImpl userService;
    @InjectMocks
    LabTestServiceImpl labTestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @DisplayName("Add test should save lab test and lab results when test is added")
    @Test
    void addTest_ShouldSaveLabTestAndLabResults_WhenTestIsAdded() {
        LabTest labTest = LabTest.builder().id(UUID.randomUUID()).build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .patientProfile(PatientProfile.builder().id(UUID.randomUUID()).build()).build();

        when(userService.getUser()).thenReturn(user);
        when(labTestRepository.findById(labTest.getId())).thenReturn(java.util.Optional.of(labTest));
        when(labTestRepository.save(labTest)).thenReturn(labTest);
        when(labResultService.saveAll(labTest.getLabResults())).thenReturn(labTest.getLabResults());

        labTestService.addTest(labTest.getId(),"test",new Date(),new ArrayList<>());

        verify(labTestRepository,times(1)).findById(labTest.getId());
        verify(labTestRepository,times(1)).save(labTest);
        verify(labTestRepository).save(argThat(labTest1 -> labTest1.getId().equals(labTest.getId())));
        verify(labResultService,times(1)).saveAll(anyList());
    }
    @DisplayName("Add test should save lab test when the test isn't added before")
    @Test
    void addTest_ShouldSaveLabTest_WhenTestIsNotAddedBefore() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .patientProfile(PatientProfile.builder().id(UUID.randomUUID()).build()).build();
        LabTest labTest = LabTest.builder().id(UUID.randomUUID()).build();
        when(userService.getUser()).thenReturn(user);
        when(labTestRepository.findById(labTest.getId())).thenReturn(java.util.Optional.empty());
        when(labTestRepository.save(any())).thenReturn(any());
        when(labResultService.saveAll(labTest.getLabResults())).thenReturn(labTest.getLabResults());

        labTestService.addTest(labTest.getId(),"test",new Date(),new ArrayList<>());

        verify(labTestRepository,times(1)).findById(labTest.getId());
        verify(labTestRepository,times(1)).save(any());
        verify(labResultService,times(1)).saveAll(anyList());
        verify(labTestRepository).save(argThat(labTest1 -> labTest1.getId()==null&&labTest1.getPatient().equals(user.getPatientProfile())));
    }

}
