package com.nexaworks.rafiq.patient.service.implementation;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.entity.model.WeightHistory;
import com.nexaworks.rafiq.patient.repository.WeightHistoryRepository;
import com.nexaworks.rafiq.patient.service.WeightHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeightHistoryServiceImpl implements WeightHistoryService {
    private final WeightHistoryRepository weightHistoryRepository;

    @Override
    public void logNewWeight(Double newWeight, Patient patient) {
        weightHistoryRepository.save(WeightHistory.builder().weight(newWeight).date(LocalDate.now())
                .patient(patient).build());
    }
}
