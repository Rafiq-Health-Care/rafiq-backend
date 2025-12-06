package com.nexaworks.rafiq.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth2 request")
public record OAuthRequest(@Schema String idToken) {
}
