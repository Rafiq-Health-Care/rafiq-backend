package com.nexaworks.rafiq.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.service.patient.WeightHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/weight-history")
@RequiredArgsConstructor
// under development
public class WeightHistoryController {
    private final WeightHistoryService weightHistoryService;

}
