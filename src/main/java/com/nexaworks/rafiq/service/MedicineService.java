package com.nexaworks.rafiq.service;

import java.util.UUID;

import com.nexaworks.rafiq.entities.Medicine;

public interface MedicineService {
    Medicine addMedicine(Medicine entity, UUID medicineId);
}
