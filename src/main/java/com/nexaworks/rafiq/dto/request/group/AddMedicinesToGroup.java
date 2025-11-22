package com.nexaworks.rafiq.dto.request.group;

import java.util.List;
import java.util.UUID;

public record AddMedicinesToGroup(List<UUID> medicineIds) {
}
