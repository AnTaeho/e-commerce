package com.example.e_commerce.coupon.controller;

import com.example.e_commerce.coupon.controller.dto.CouponRequest;
import com.example.e_commerce.coupon.controller.dto.CouponResponse;
import com.example.e_commerce.coupon.service.CouponService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<CouponResponse> createTicket(@RequestBody CouponRequest couponRequest) {
        return ResponseEntity.ok(couponService.createCoupon(couponRequest));
    }

    @PostMapping("/{couponId}")
    public ResponseEntity<Void> issueCoupon(@PathVariable("couponId") Long couponId) {
        String email = UUID.randomUUID().toString();
        couponService.issueCouponWithRedisTransaction(couponId, email);
        return ResponseEntity.ok(null);
    }
}
