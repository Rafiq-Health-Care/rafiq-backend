package com.nexaworks.rafiq.medication.service.implementation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.medication.api.dto.request.BulkMedicineOperationRequest;
import com.nexaworks.rafiq.medication.api.dto.request.MedicineFilter;
import com.nexaworks.rafiq.medication.api.dto.request.UpdateMedicinePatchRequest;
import com.nexaworks.rafiq.medication.entity.enums.MedicineStatus;
import com.nexaworks.rafiq.medication.entity.model.Drug;
import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.medication.exception.*;
import com.nexaworks.rafiq.medication.repository.MedicineRepository;
import com.nexaworks.rafiq.medication.repository.specification.MedicineSpecification;
import com.nexaworks.rafiq.medication.service.DrugService;
import com.nexaworks.rafiq.medication.service.GroupService;
import com.nexaworks.rafiq.medication.service.MedicineService;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineServiceImpl implements MedicineService {
    private final MedicineRepository medicineRepository;
    private final DrugService drugService;
    private final GroupService groupService;

    @Override
    @Transactional
    public Medicine addMedicine(Medicine entity, UUID drugId, UUID patientId) {

        entity.setPatientId(patientId);
        if (medicineRepository.existsByPatientIdAndDrugId(patientId, drugId)) {
            throw new MedicineAlreadyExist("Medicine already exist");
        }
        if (medicineRepository.countByPatientId(patientId) >= 200) {
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
    public Page<Medicine> getAllMedicines(Pageable pageable, MedicineFilter filter,
            UUID patientId) {

        return medicineRepository.findAll(MedicineSpecification.filter(filter, patientId),
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Medicine getMedicineById(UUID medicineId, UUID patientId) {
        return getMedicine(medicineId, patientId);
    }

    @Override
    @Transactional(noRollbackFor = {CannotDeleteMedicine.class})
    public void deleteMedicine(UUID medicineId, UUID patientId) {
        Medicine medicine = getMedicine(medicineId, patientId);
        if (medicine.getReminder() != null && !medicine.getReminder().getReminderLogs().isEmpty()) {
            medicine.setStatus(MedicineStatus.INACTIVE);
            medicineRepository.save(medicine);
            throw new CannotDeleteMedicine(
                    "Medicine has reminder logs associated with it and cannot be deleted");
        }
        medicineRepository.delete(medicine);
    }

    @Override
    @Transactional
    public Medicine updateMedicine(Medicine entity, UUID medicineId, UUID patientId) {
        Medicine medicine = getMedicine(medicineId, patientId);
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
    public Medicine updateSpecific(UUID medicineId, UpdateMedicinePatchRequest request,
            UUID patientId) {
        Medicine medicine = getMedicine(medicineId, patientId);
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
    private Medicine getMedicine(UUID medicineId, UUID patientId) {
        Medicine medicine = medicineRepository.findById(medicineId).orElseThrow(
                () -> new MedicineNotFound("Medicine not found with id: " + medicineId));
        if (!medicine.getPatientId().equals(patientId)) {
            throw new MedicineNotFound("Medicine not found with id: " + medicineId);
        }
        return medicine;
    }

    @Override
    @Transactional
    public List<UUID> bulkMedicineOperation(BulkMedicineOperationRequest request, UUID patientId)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (request.medicineIds().isEmpty()) {
            throw new ValidationException("Medicine ids cannot be empty");
        }
        List<UUID> failedIds = new ArrayList<>();
        Method action = this.getClass().getMethod(request.action().getAction(), List.class,
                Optional.class, List.class, UUID.class);
        action.invoke(this, request.medicineIds(), request.groupId(), failedIds, patientId);

        return failedIds;
    }
    @Transactional
    public void delete(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds,
            UUID patientId) {
        ids.forEach(medicineId -> {
            try {
                deleteMedicine(medicineId, patientId);
            } catch (Exception e) {
                failedIds.add(medicineId);
            }
        });

    }
    @Transactional
    public void moveToGroup(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds,
            UUID patientId) {
        groupId.orElseThrow(() -> new GroupNotFoundException("Group id is required"));
        Group group = groupService.getGroupById(groupId.get(), patientId);
        if (!group.getPatientId().equals(patientId)) {
            throw new GroupNotFoundException("Medicine not found with id: " + ids);
        }
        ids.forEach(medicineId -> {
            try {
                Medicine medicine = getMedicine(medicineId, patientId);
                medicine.setGroup(group);
                medicineRepository.save(medicine);
            } catch (Exception e) {
                failedIds.add(medicineId);
            }
        });
    }
    @Transactional
    public void markActive(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds,
            UUID patientId) {
        ids.forEach(medicineId -> {
            try {
                Medicine medicine = getMedicine(medicineId, patientId);
                medicine.setStatus(MedicineStatus.ACTIVE);
                medicineRepository.save(medicine);
            } catch (Exception e) {
                failedIds.add(medicineId);
            }
        });

    }
    @Transactional
    public void markInActive(List<UUID> ids, Optional<UUID> groupId, List<UUID> failedIds,
            UUID patientId) {
        ids.forEach(medicineId -> {
            try {
                Medicine medicine = getMedicine(medicineId, patientId);
                medicine.setStatus(MedicineStatus.INACTIVE);
                medicineRepository.save(medicine);
            } catch (Exception e) {
                failedIds.add(medicineId);
            }
        });

    }

}
