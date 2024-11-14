package com.example.e_commerce.coupon.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record CouponRequest(
        int amount,
        int discount,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate expiredAt
) {
}
