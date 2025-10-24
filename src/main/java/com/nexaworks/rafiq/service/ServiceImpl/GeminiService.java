package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService implements AiService {

    @Override
    public String extractLabResultsFromPdf(MultipartFile pdfFile) {
        return "";
    }
}
