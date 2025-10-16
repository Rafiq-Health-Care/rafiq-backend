package com.nexaworks.rafiq.dto.response;

import java.util.List;

public record LoginResponse(List<String> roles,String refreshToken) {
}
