package com.nexaworks.rafiq.controller;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.nexaworks.rafiq.dto.request.labTest.TestResultRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.labTest.TestResponse;
import com.nexaworks.rafiq.dto.response.labTest.TestResultsResponse;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.mapper.ResultMapper;
import com.nexaworks.rafiq.mapper.TestMapper;
import com.nexaworks.rafiq.service.file.PdfExtractorService;
import com.nexaworks.rafiq.service.labReports.LabTestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lab-test")
@RequiredArgsConstructor
@Tag(name = "Lab Test Management", description = "Endpoints for lab test results, uploads, and PDF extraction")
public class LabTestController {
    private final PdfExtractorService pdfExtractorService;
    private final LabTestService labTestService;
    private final ResultMapper resultMapper;
    private final PageMapper pageMapper;
    private final TestMapper testMapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload lab test PDF", description = "Extracts and parses lab test data from a PDF file.")
    @ApiResponse(responseCode = "200", description = "PDF extracted successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file)
            throws IOException, DocumentException, ExecutionException, InterruptedException {
        return ResponseEntity.ok().body(pdfExtractorService.extractPdf(file));
    }

    @PostMapping(value = "/test-results")
    @Operation(summary = "Add test results", description = "Saves lab test results manually or from extracted PDF data.")
    @ApiResponse(responseCode = "200", description = "Test results saved successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> testResults(
            @RequestBody @Valid TestResultRequest testResultRequest) {

        labTestService.addTest(testResultRequest.testId(), testResultRequest.name(),
                testResultRequest.date(), resultMapper.toEntity(testResultRequest.tests()));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get all tests", description = "Retrieves paginated list of lab tests with sorting.")
    @ApiResponse(responseCode = "200", description = "Tests retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<TestResponse>> getAllTests(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        return ResponseEntity.ok().body(
                pageMapper.mapToTestResponse(labTestService.getAll(page, size, sort, direction)));
    }

    @GetMapping("/{test-id}")
    @Operation(summary = "Get test by ID", description = "Retrieves detailed lab test results for a specific test.")
    @ApiResponse(responseCode = "200", description = "Test retrieved successfully", content = @Content(schema = @Schema(implementation = TestResultsResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TestResultsResponse> getTest(@PathVariable("test-id") UUID testId) {
        return ResponseEntity.ok()
                .body(testMapper.mapToTestResponse(labTestService.getTest(testId)));
    }

    @DeleteMapping("/{test-id}")
    @Operation(summary = "Delete test", description = "Removes a lab test from the record.")
    @ApiResponse(responseCode = "200", description = "Test deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteTest(@PathVariable("test-id") UUID testId) {
        labTestService.deleteTest(testId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "Delete all tests", description = "Removes all lab tests for the user. Returns count of deleted tests.")
    @ApiResponse(responseCode = "200", description = "All tests deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Integer> deleteAllTests() {
        return ResponseEntity.ok().body(labTestService.deleteAll());
    }

    @PutMapping("/update/{test-id}")
    @Operation(summary = "Update test", description = "Updates existing lab test results.")
    @ApiResponse(responseCode = "200", description = "Test updated successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> updateTest(@RequestBody @Valid TestResultRequest testResultRequest,
            @PathVariable("test-id") UUID testId) {
        labTestService.update(testId, testResultRequest,
                resultMapper.toEntity(testResultRequest.tests()));
        return ResponseEntity.ok().build();
    }
}
