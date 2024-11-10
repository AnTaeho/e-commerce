package com.example.e_commerce.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCouponService {

    private static final String COUPON_COUNT = "coupon_count:";

    private final RedisTemplate<String, String> redisTemplate;

    public void increment(Long couponId) {
        redisTemplate
                .opsForValue()
                .increment(makeCouponCount(couponId));
    }

    public Long addKeyToSet(String value) {
        return redisTemplate
                .opsForSet()
                .add("applied-user", value);
    }

    public boolean isNotServed(Long couponId) {
        return redisTemplate
                .opsForValue()
                .get(makeCouponCount(couponId)) == null;
    }

    public Long decrease(Long couponId) {
        return redisTemplate
                .opsForValue()
                .decrement(makeCouponCount(couponId));
    }

    public void setTicketCount(Long couponId, int amount) {
        redisTemplate
                .opsForValue()
                .set(makeCouponCount(couponId), String.valueOf(amount));
    }

    private String makeCouponCount(Long couponId) {
        return COUPON_COUNT + couponId;
    }

    public Long getAmount(Long couponId) {
        String result = redisTemplate.opsForValue()
                .get("coupon:" + couponId);
        if (result == null) {
            return null;
        }
        return Long.parseLong(result);
    }
}
