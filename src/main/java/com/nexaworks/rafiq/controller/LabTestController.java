package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.service.PdfExtractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/lab-test")
@RequiredArgsConstructor
public class LabTestController {
    private final PdfExtractorService  pdfExtractorService;
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<String > upload(@RequestParam("file") MultipartFile file) {
        return  ResponseEntity.ok().body(
            pdfExtractorService.extractPdf(file)
        );
    }
}
