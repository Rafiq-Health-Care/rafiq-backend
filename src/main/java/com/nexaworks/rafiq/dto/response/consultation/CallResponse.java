package com.nexaworks.rafiq.dto.response.consultation;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing call credentials to join the consultation")
public record CallResponse(

        @Schema(description = "Channel name to join the call", example = "123e4567-e89b-12d3-a456-426614174000") UUID channelName,

        @Schema(description = "Access token to authenticate into the call", example = "eyJhbGciOiJIUzI1NiJ9...") String token) {
}
