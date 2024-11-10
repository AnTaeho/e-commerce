package com.example.e_commerce.user.controller.dto;

public record JoinRequest (
        String username,
        String email,
        String password
) {
}
