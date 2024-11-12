package com.example.e_commerce.coupon.controller.dto;

public record RedisResult(
        Long amount,
        Long size,
        boolean alreadyIn
) {
}
