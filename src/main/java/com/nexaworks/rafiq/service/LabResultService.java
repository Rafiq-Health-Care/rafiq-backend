package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.LabResult;
import com.nexaworks.rafiq.entities.LabTest;

import java.util.List;

public interface LabResultService {
   List<LabResult> saveAll(List<LabResult> entity);
}
