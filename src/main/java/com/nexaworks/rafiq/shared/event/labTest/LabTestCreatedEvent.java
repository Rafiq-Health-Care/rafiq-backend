package com.nexaworks.rafiq.shared.event.labTest;

import java.util.UUID;

public record LabTestCreatedEvent(UUID fileId, UUID testId) {
}
