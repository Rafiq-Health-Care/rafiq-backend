package com.nexaworks.rafiq.shared.event.labTest;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.shared.dto.TestRequest;

public record LabTestCreatedEvent(UUID fileId, UUID testId, List<TestRequest> tests) {
}
