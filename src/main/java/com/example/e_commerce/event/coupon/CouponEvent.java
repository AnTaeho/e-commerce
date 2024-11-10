package com.example.e_commerce.event.coupon;

public record CouponEvent(
        Long couponId,
        String email
) {
}
