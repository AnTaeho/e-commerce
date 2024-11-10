package com.example.e_commerce.user.controller.dto;

public record LoginRequest (
        String email,
        String password
) {
}
