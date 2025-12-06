package com.nexaworks.rafiq.labTest.service;

import java.util.List;

import com.nexaworks.rafiq.labTest.entity.LabResult;

public interface LabResultService {
    List<LabResult> saveAll(List<LabResult> entity);

    void deleteAll(List<LabResult> labResults);
}
