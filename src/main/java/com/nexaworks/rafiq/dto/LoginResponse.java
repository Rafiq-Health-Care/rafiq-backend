package com.nexaworks.rafiq.dto;

import java.util.List;

public record LoginResponse(List<String> roles,String jwtToken,String refreshToken) {
}
