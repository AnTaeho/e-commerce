package com.example.e_commerce.coupon.repository;

import com.example.e_commerce.coupon.controller.dto.RedisResult;
import com.example.e_commerce.coupon.controller.dto.RedisVo;
import com.example.e_commerce.coupon.redis.RedisOperation;
import com.example.e_commerce.coupon.redis.RedisTransaction;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTransaction redisTransaction;
    private final RedisOperation<RedisVo> operation;

    public RedisResult execute(RedisVo redisVO) {
        List<Object> execute = redisTransaction.execute(redisTemplate, operation, redisVO);
        List<Long> result = new ArrayList<>();
        for (Object o : execute) {
            result.add((Long) o);
        }
        return new RedisResult(result.get(0), result.get(1) == 1L);
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
