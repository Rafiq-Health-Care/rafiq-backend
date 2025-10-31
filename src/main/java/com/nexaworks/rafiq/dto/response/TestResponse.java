package com.nexaworks.rafiq.dto.response;




import java.util.Date;
import java.util.UUID;

public record TestResponse(String name,
                           UUID labId,
                           String labName,
                           UUID testId,
                           String fileUrl,
                           String fileType) {
}
