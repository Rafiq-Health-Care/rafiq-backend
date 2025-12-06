package com.nexaworks.rafiq.shared.mapper;

import com.nexaworks.rafiq.labTest.entity.LabTest;
import com.nexaworks.rafiq.lab.entity.Lab;
import com.nexaworks.rafiq.medication.entity.model.Drug;
import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.medication.mapper.DrugMapper;
import com.nexaworks.rafiq.medication.mapper.GroupMapper;
import com.nexaworks.rafiq.medication.mapper.MedicineMapper;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.medication.api.dto.response.AddGroupResponse;
import com.nexaworks.rafiq.shared.dto.PageResponse;
import com.nexaworks.rafiq.lab.api.dto.LabResponse;
import com.nexaworks.rafiq.labTest.api.dto.TestResponse;
import com.nexaworks.rafiq.medication.api.dto.response.DrugSearchResponse;
import com.nexaworks.rafiq.medication.api.dto.response.MedicineGroupResponse;

@Mapper(componentModel = "spring", uses = {DrugMapper.class, MedicineMapper.class,
        GroupMapper.class})
public interface PageMapper {

    @Mapping(target = "content", expression = "java(all.getContent().stream().map(lab -> new LabResponse(lab.getId(), lab.getName(), lab.getLogo())).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "numberOfElements", expression = "java((int) all.getTotalElements())")
    @Mapping(target = "size", expression = "java(all.getSize())")
    @Mapping(target = "totalPages", expression = "java(all.getTotalPages())")
    @Mapping(target = "lastPage", expression = "java(all.isLast())")
    @Mapping(target = "firstPage", expression = "java(all.isFirst())")
    PageResponse<LabResponse> mapToLabPage(Page<Lab> all);

    @Mapping(target = "content", expression = "java(all.getContent().stream().map(labTest -> new TestResponse(labTest.getName(), labTest.getId(), labTest.getPdf(), labTest.getFileType())).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "numberOfElements", expression = "java((int) all.getTotalElements())")
    @Mapping(target = "size", expression = "java(all.getSize())")
    @Mapping(target = "totalPages", expression = "java(all.getTotalPages())")
    @Mapping(target = "lastPage", expression = "java(all.isLast())")
    @Mapping(target = "firstPage", expression = "java(all.isFirst())")
    PageResponse<TestResponse> mapToTestResponse(Page<LabTest> all);
    @Mapping(target = "content", expression = "java(all.getContent().stream().map(drugMapper::toDto).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "numberOfElements", expression = "java((int) all.getTotalElements())")
    @Mapping(target = "size", expression = "java(all.getSize())")
    @Mapping(target = "totalPages", expression = "java(all.getTotalPages())")
    @Mapping(target = "lastPage", expression = "java(all.isLast())")
    @Mapping(target = "firstPage", expression = "java(all.isFirst())")
    PageResponse<DrugSearchResponse> mapToDrugSearchResponsePage(Page<Drug> all,
            @Context DrugMapper drugMapper);

    @Mapping(target = "content", expression = "java(all.getContent().stream().map(medicineMapper::toGroupDto).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "numberOfElements", expression = "java((int) all.getTotalElements())")
    @Mapping(target = "size", expression = "java(all.getSize())")
    @Mapping(target = "totalPages", expression = "java(all.getTotalPages())")
    @Mapping(target = "lastPage", expression = "java(all.isLast())")
    @Mapping(target = "firstPage", expression = "java(all.isFirst())")
    PageResponse<MedicineGroupResponse> mapToMedicinePage(Page<Medicine> all,
            @Context MedicineMapper medicineMapper);

    @Mapping(target = "content", expression = "java(all.getContent().stream().map(groupMapper::toDto).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "numberOfElements", expression = "java((int) all.getTotalElements())")
    @Mapping(target = "size", expression = "java(all.getSize())")
    @Mapping(target = "totalPages", expression = "java(all.getTotalPages())")
    @Mapping(target = "lastPage", expression = "java(all.isLast())")
    @Mapping(target = "firstPage", expression = "java(all.isFirst())")
    PageResponse<AddGroupResponse> mapToGroupPage(Page<Group> all,
            @Context GroupMapper groupMapper);

}
