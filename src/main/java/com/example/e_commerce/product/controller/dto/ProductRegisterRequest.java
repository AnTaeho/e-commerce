package com.example.e_commerce.product.controller.dto;

public record ProductRegisterRequest(
        String name,
        int price,
        int amount
) {
}
