package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.feedback.AddFeedbackRequest;
import com.nexaworks.rafiq.service.feedback.IFeedbackService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final IFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Void> addFeedback(@Valid @RequestBody AddFeedbackRequest request) {
        feedbackService.addFeedback(request);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable UUID feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();

    }

}
