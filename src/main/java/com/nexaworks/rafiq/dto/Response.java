package com.nexaworks.rafiq.dto;

public record Response<T>(Boolean success, T data) {
}
