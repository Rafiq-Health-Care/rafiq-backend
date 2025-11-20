package com.nexaworks.rafiq.dto.response.medicine;

public record AddResponse<T>(boolean success, String message, T data) {
}
