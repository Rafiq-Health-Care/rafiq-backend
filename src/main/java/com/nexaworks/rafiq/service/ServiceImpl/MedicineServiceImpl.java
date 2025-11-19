package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.enums.MedicineStatus;
import com.nexaworks.rafiq.exception.custom.MedicineAlreadyExist;
import com.nexaworks.rafiq.exception.custom.MedicineLimit;
import com.nexaworks.rafiq.repository.MedicineRepository;
import com.nexaworks.rafiq.service.DrugService;
import com.nexaworks.rafiq.service.MedicineService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineServiceImpl implements MedicineService {
    private final MedicineRepository medicineRepository;
    private final DrugService drugService;
    private final PatientServiceImpl patientService;
    @Override
    @Transactional
    public Medicine addMedicine(Medicine entity, UUID drugId) {

        PatientProfile patient = patientService.getPatientProfile();
        entity.setPatient(patient);
        if (medicineRepository.existsByPatientIdAndDrugId(patient.getId(), drugId)) {
            throw new MedicineAlreadyExist("Medicine already exist");
        }
        if (medicineRepository.countByPatientId(patient.getId()) >= 200) {
            throw new MedicineLimit("You have reached the limit of medicine");
        }
        Drug drug = drugService.getDrugById(drugId);
        entity.setDrug(drug);
        entity.setStatus(MedicineStatus.ACTIVE);
        medicineRepository.save(entity);
        return entity;
    }

}
