package com.nexaworks.rafiq.controller;

import com.itextpdf.text.DocumentException;
import com.nexaworks.rafiq.dto.request.TestResultRequest;
import com.nexaworks.rafiq.dto.response.PageResponse;
import com.nexaworks.rafiq.dto.response.TestResponse;
import com.nexaworks.rafiq.dto.response.TestResultsResponse;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.mapper.ResultMapper;
import com.nexaworks.rafiq.mapper.TestMapper;
import com.nexaworks.rafiq.service.LabTestService;
import com.nexaworks.rafiq.service.PdfExtractorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/lab-test")
@RequiredArgsConstructor
public class LabTestController {
    private final PdfExtractorService  pdfExtractorService;
    private final LabTestService labTestService;
    private final ResultMapper resultMapper;
    private final PageMapper pageMapper;
    private final TestMapper testMapper;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<String > upload(@RequestParam("file") MultipartFile file) throws IOException, DocumentException, ExecutionException, InterruptedException {
        return  ResponseEntity.ok().body(
            pdfExtractorService.extractPdf(file)
        );
    }
    @PostMapping(value = "/test-results")
    public ResponseEntity<Void> testResults(@RequestBody @Valid TestResultRequest testResultRequest
                                           ){

        labTestService.addTest(testResultRequest, resultMapper.toEntity(testResultRequest.tests()));
        return ResponseEntity.ok().build();
    }
    @GetMapping
    public ResponseEntity<PageResponse<TestResponse>> getAllTests(
            @RequestParam(value= "page",defaultValue = "0")int page,
            @RequestParam(value= "size",defaultValue = "10")int size,
            @RequestParam(value= "sort",defaultValue = "name")String sort,
            @RequestParam(value= "direction",defaultValue = "asc")String direction
    ){
        return ResponseEntity.ok().body(pageMapper.mapToTestResponse(labTestService.getAll(page,
                size,sort,direction)));
    }
    @GetMapping("/{test-id}")
    public ResponseEntity<TestResultsResponse> getTest(@PathVariable("test-id") UUID testId){
        return ResponseEntity.ok().body(testMapper.mapToTestResponse(labTestService.getTest(testId)));
    }
    @DeleteMapping("/{test-id}")
    public ResponseEntity<Void> deleteTest(@PathVariable("test-id")UUID testId){
        labTestService.deleteTest(testId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping
    public ResponseEntity<Integer> deleteAllTests(){
        return ResponseEntity.ok().body(labTestService.deleteAll());
    }
    @PutMapping("/update/{test-id}")
    public ResponseEntity<Void> updateTest(@RequestBody @Valid TestResultRequest testResultRequest,
                                           @PathVariable("test-id") UUID testId){
        labTestService.update(testId,testResultRequest,resultMapper.toEntity(testResultRequest.tests()));
        return ResponseEntity.ok().build();
    }
}
