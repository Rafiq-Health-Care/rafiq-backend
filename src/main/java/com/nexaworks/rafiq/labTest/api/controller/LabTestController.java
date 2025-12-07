package com.nexaworks.rafiq.labTest.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.labTest.api.dto.TestResponse;
import com.nexaworks.rafiq.labTest.api.dto.TestResultRequest;
import com.nexaworks.rafiq.labTest.api.dto.TestResultsResponse;
import com.nexaworks.rafiq.labTest.mapper.ResultMapper;
import com.nexaworks.rafiq.labTest.mapper.TestMapper;
import com.nexaworks.rafiq.labTest.service.LabTestService;
import com.nexaworks.rafiq.shared.dto.PageResponse;
import com.nexaworks.rafiq.shared.mapper.PageMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lab-test")
@RequiredArgsConstructor
public class LabTestController {
    private final LabTestService labTestService;
    private final ResultMapper resultMapper;
    private final PageMapper pageMapper;
    private final TestMapper testMapper;

    @PostMapping(value = "/test-results")
    public ResponseEntity<Void> testResults(@RequestBody @Valid TestResultRequest testResultRequest,
            Authentication authentication) {

        labTestService.addTest(testResultRequest.testId(), testResultRequest.name(),
                testResultRequest.date(), resultMapper.toEntity(testResultRequest.tests()),
                (UUID) authentication.getPrincipal());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<TestResponse>> getAllTests(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Authentication authentication) {
        return ResponseEntity.ok().body(pageMapper.mapToTestResponse(labTestService.getAll(page,
                size, sort, direction, (UUID) authentication.getPrincipal()), testMapper));
    }

    @GetMapping("/{test-id}")
    public ResponseEntity<TestResultsResponse> getTest(@PathVariable("test-id") UUID testId,
            Authentication authentication) {
        return ResponseEntity.ok().body(testMapper.mapToTestResponse(
                labTestService.getTest(testId, (UUID) authentication.getPrincipal())));
    }

    @DeleteMapping("/{test-id}")
    public ResponseEntity<Void> deleteTest(@PathVariable("test-id") UUID testId,
            Authentication authentication) {
        labTestService.deleteTest(testId, (UUID) authentication.getPrincipal());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Integer> deleteAllTests(Authentication authentication) {
        return ResponseEntity.ok()
                .body(labTestService.deleteAll((UUID) authentication.getPrincipal()));
    }

    @PutMapping("/update/{test-id}")
    public ResponseEntity<Void> updateTest(@RequestBody @Valid TestResultRequest testResultRequest,
            @PathVariable("test-id") UUID testId, Authentication authentication) {
        labTestService.update(testId, testResultRequest,
                resultMapper.toEntity(testResultRequest.tests()),
                (UUID) authentication.getPrincipal());
        return ResponseEntity.ok().build();
    }
}
