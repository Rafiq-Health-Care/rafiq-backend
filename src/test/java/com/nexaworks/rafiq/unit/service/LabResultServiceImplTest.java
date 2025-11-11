package com.nexaworks.rafiq.unit.service;

import com.nexaworks.rafiq.entities.LabResult;
import com.nexaworks.rafiq.repository.LabResultRepository;
import com.nexaworks.rafiq.service.ServiceImpl.LabResultServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("LabResultService Test Cases")
@ExtendWith(MockitoExtension.class)
public class LabResultServiceImplTest {
    @Mock
    LabResultRepository labResultRepository;
    @InjectMocks
    LabResultServiceImpl labResultService;
    @DisplayName("Save all lab results")
    @Test
    void shouldSaveAllLabResults(){
        when(labResultRepository.saveAll(anyList())).thenReturn(List.of(new LabResult()));
        labResultService.saveAll(List.of(new LabResult()));
        verify(labResultRepository, times(1)).saveAll(anyList());


    }
    @DisplayName("Delete all lab results")
    @Test
    void shouldDeleteAllLabResults(){
        doNothing().when(labResultRepository).deleteAll(anyList());
        labResultService.deleteAll(List.of(new LabResult()));
        verify(labResultRepository, times(1)).deleteAll(anyList());
    }
}
