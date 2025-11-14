package com.nexaworks.rafiq.service;

import java.util.List;

import com.nexaworks.rafiq.entities.LabResult;

public interface LabResultService {
    List<LabResult> saveAll(List<LabResult> entity);

    void deleteAll(List<LabResult> labResults);
}
