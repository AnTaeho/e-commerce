package com.example.e_commerce.coupon.controller.dto;

import java.time.LocalDate;

public record CouponRequest(
        int amount,
        int discount,
        LocalDate expiredAt
) {
}
