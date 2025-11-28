package com.nexaworks.rafiq.service.ServiceImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.medicine.BulkMedicineOperationRequest;
import com.nexaworks.rafiq.dto.request.medicine.MedicineFilter;
import com.nexaworks.rafiq.dto.request.medicine.UpdateMedicinePatchRequest;
import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.exception.custom.GroupNotFoundException;
import com.nexaworks.rafiq.exception.custom.MedicineAlreadyExist;
import com.nexaworks.rafiq.exception.custom.MedicineLimit;
import com.nexaworks.rafiq.exception.custom.MedicineNotFound;
import com.nexaworks.rafiq.repository.MedicineRepository;
import com.nexaworks.rafiq.service.DrugService;
import com.nexaworks.rafiq.service.GroupService;
import com.nexaworks.rafiq.service.MedicineService;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.specification.MedicineSpecification;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineServiceImpl implements MedicineService {
    private final MedicineRepository medicineRepository;
    private final DrugService drugService;
    private final PatientService patientService;
    private final GroupService groupService;

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
        entity.setName(drug.getTradeName());
        entity.setStatus(MedicineStatus.ACTIVE);
        medicineRepository.save(entity);
        return entity;
    }

    @Override
    public Page<Medicine> getAllMedicines(Pageable pageable, MedicineFilter filter) {
        PatientProfile patient = patientService.getPatientProfile();
        return medicineRepository.findAll(MedicineSpecification.filter(filter, patient.getId()),
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Medicine getMedicineById(UUID medicineId) {
        PatientProfile patient = patientService.getPatientProfile();

        return getMedicine(medicineId, patient);
    }

    @Override
    @Transactional
    public void deleteMedicine(UUID medicineId) {
        Medicine medicine = getMedicine(medicineId, patientService.getPatientProfile());
        medicineRepository.delete(medicine);
    }

    @Override
    @Transactional
    public Medicine updateMedicine(Medicine entity, UUID medicineId) {
        Medicine medicine = getMedicine(medicineId, patientService.getPatientProfile());
        medicine.setDosage(entity.getDosage());
        medicine.setFrequency(entity.getFrequency());
        medicine.setStartDate(entity.getStartDate());
        medicine.setEndDate(entity.getEndDate());
        medicine.setNotes(entity.getNotes());
        medicine.setType(entity.getType());
        medicine.setStatus(entity.getStatus());
        medicine.setName(entity.getName());
        medicine.setReminderFrequency(entity.getReminderFrequency());
        medicine.setCustomDays(entity.getCustomDays());
        return medicineRepository.save(medicine);
    }

    @Override
    @Transactional
    public Medicine updateSpecific(UUID medicineId, UpdateMedicinePatchRequest request)
            throws SchedulerException {
        Medicine medicine = getMedicine(medicineId, patientService.getPatientProfile());
        request.dosage().ifPresent(medicine::setDosage);
        request.frequency().ifPresent(medicine::setFrequency);
        request.startDate().ifPresent(medicine::setStartDate);
        request.endDate().ifPresent(medicine::setEndDate);
        request.notes().ifPresent(medicine::setNotes);
        request.type().ifPresent(medicine::setType);
        request.status().ifPresent(medicine::setStatus);
        request.name().ifPresent(medicine::setName);
        request.reminderFrequency().ifPresent(medicine::setReminderFrequency);
        request.customDays().ifPresent(medicine::setCustomDays);
        return medicineRepository.save(medicine);
    }

    @NotNull
    private Medicine getMedicine(UUID medicineId, PatientProfile patientService) {
        Medicine medicine = medicineRepository.findById(medicineId).orElseThrow(
                () -> new MedicineNotFound("Medicine not found with id: " + medicineId));
        if (!medicine.getPatient().getId().equals(patientService.getId())) {
            throw new MedicineNotFound("Medicine not found with id: " + medicineId);
        }
        return medicine;
    }

    @Override
    @Transactional
    public List<UUID> bulkMedicineOperation(BulkMedicineOperationRequest request)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (request.medicineIds().isEmpty()) {
            throw new ValidationException("Medicine ids cannot be empty");
        }
        List<UUID> failedIds = new ArrayList<>();
        Method action = this.getClass().getMethod(request.action().getAction(), List.class,
                Optional.class, List.class);
        action.invoke(this, request.medicineIds(), request.groupId(), failedIds);

        return failedIds;
    }
    @Transactional
    public void delete(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds) {
        ids.forEach(medicineId -> {
            try {
                deleteMedicine(medicineId);
            } catch (Exception e) {
                failedIds.add(medicineId);
            }
        });

    }
    @Transactional
    public void moveToGroup(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds) {
        groupId.orElseThrow(() -> new GroupNotFoundException("Group id is required"));
        Group group = groupService.getGroupById(groupId.get());
        PatientProfile patient = patientService.getPatientProfile();
        if (!group.getPatient().getId().equals(patient.getId())) {
            throw new GroupNotFoundException("Medicine not found with id: " + ids);
        }
        ids.forEach(medicineId -> {
            try {
                Medicine medicine = getMedicine(medicineId, patient);
                medicine.setGroup(group);
                medicineRepository.save(medicine);
            } catch (Exception e) {
                failedIds.add(medicineId);
            }
        });
    }
    @Transactional
    public void markActive(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds) {
        PatientProfile patient = patientService.getPatientProfile();
        ids.forEach(medicineId -> {
            try {
                Medicine medicine = getMedicine(medicineId, patient);
                medicine.setStatus(MedicineStatus.ACTIVE);
                medicineRepository.save(medicine);
            } catch (Exception e) {
                failedIds.add(medicineId);
            }
        });

    }
    @Transactional
    public void markInActive(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds) {
        PatientProfile patient = patientService.getPatientProfile();
        ids.forEach(medicineId -> {
            try {
                Medicine medicine = getMedicine(medicineId, patient);
                medicine.setStatus(MedicineStatus.INACTIVE);
                medicineRepository.save(medicine);
            } catch (Exception e) {
                failedIds.add(medicineId);
            }
        });

    }

}
