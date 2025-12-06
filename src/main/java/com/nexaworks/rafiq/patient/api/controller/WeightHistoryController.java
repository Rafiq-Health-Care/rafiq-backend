package com.nexaworks.rafiq.patient.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.patient.service.WeightHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/weight-history")
@RequiredArgsConstructor
public class WeightHistoryController {
    private final WeightHistoryService weightHistoryService;

}
