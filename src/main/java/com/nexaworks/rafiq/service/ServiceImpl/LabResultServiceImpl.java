package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.LabResult;
import com.nexaworks.rafiq.repository.LabResultRepository;
import com.nexaworks.rafiq.service.LabResultService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabResultServiceImpl implements LabResultService {
    private final LabResultRepository labResultRepository;

    @Override
    @Transactional
    public List<LabResult> saveAll(List<LabResult> entity) {
        return labResultRepository.saveAll(entity);
    }

    @Override
    @Transactional
    public void deleteAll(List<LabResult> labResults) {
        labResultRepository.deleteAll(labResults);
    }
}
