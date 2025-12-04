package com.nexaworks.rafiq.service.ServiceImpl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.WeightHistory;
import com.nexaworks.rafiq.repository.WeightHistoryRepository;
import com.nexaworks.rafiq.service.WeightHistoryService;

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
