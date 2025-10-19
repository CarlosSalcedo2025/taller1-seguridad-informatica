package com.example.taller1.dto;

public record RegisterRequest(
        String name,
        String email,
        String password,
        boolean admin
) {
}
