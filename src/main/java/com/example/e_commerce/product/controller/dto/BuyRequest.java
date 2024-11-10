package com.example.e_commerce.product.controller.dto;

public record BuyRequest(
        Long productId,
        int amount
) {
}
