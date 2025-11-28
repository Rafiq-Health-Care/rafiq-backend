package com.nexaworks.rafiq.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.lab.LabResponse;
import com.nexaworks.rafiq.dto.response.labTest.TestResponse;
import com.nexaworks.rafiq.dto.response.medicine.DrugSearchResponse;
import com.nexaworks.rafiq.dto.response.medicine.MedicineGroupResponse;
import com.nexaworks.rafiq.entities.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PageMapper {
    private final DrugMapper drugMapper;
    private final MedicineMapper medicineMapper;
    private final GroupMapper groupMapper;
    public PageResponse<LabResponse> mapToLabPage(Page<Lab> all) {
        List<LabResponse> content = all.getContent().stream()
                .map(lab -> new LabResponse(lab.getId(), lab.getName(), lab.getLogo()))
                .collect(Collectors.toList());
        return new PageResponse<>(content, (int) all.getTotalElements(), all.getSize(),
                all.getTotalPages(), all.isLast(), all.isFirst());
    }

    public PageResponse<TestResponse> mapToTestResponse(Page<LabTest> all) {
        List<TestResponse> content = all
                .getContent().stream().map(labTest -> new TestResponse(labTest.getName(),
                        labTest.getId(), labTest.getPdf(), labTest.getFileType()))
                .collect(Collectors.toList());
        return new PageResponse<>(content, (int) all.getTotalElements(), all.getSize(),
                all.getTotalPages(), all.isLast(), all.isFirst());
    }
    public PageResponse<DrugSearchResponse> mapToDrugSearchResponsePage(Page<Drug> all) {

        return new PageResponse<>(all.getContent().stream().map(drugMapper::toDto).toList(),
                (int) all.getTotalElements(), all.getSize(), all.getTotalPages(), all.isLast(),
                all.isFirst());
    }

    public PageResponse<MedicineGroupResponse> mapToMedicinePage(Page<Medicine> all) {
        return new PageResponse<>(
                all.getContent().stream().map(medicineMapper::toGroupDto).toList(),
                (int) all.getTotalElements(), all.getSize(), all.getTotalPages(), all.isLast(),
                all.isFirst());
    }

    public PageResponse<AddGroupResponse> mapToGroupPage(Page<Group> groups) {
        List<AddGroupResponse> content = groups.getContent().stream().map(groupMapper::toDto)
                .collect(Collectors.toList());
        return new PageResponse<>(content, (int) groups.getTotalElements(), groups.getSize(),
                groups.getTotalPages(), groups.isLast(), groups.isFirst());
    }

}
