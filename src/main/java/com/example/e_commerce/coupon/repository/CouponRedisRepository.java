package com.example.e_commerce.coupon.repository;

import com.example.e_commerce.coupon.controller.dto.RedisResult;
import com.example.e_commerce.coupon.controller.dto.RedisVo;
import com.example.e_commerce.coupon.redis.RedisOperation;
import com.example.e_commerce.coupon.redis.RedisTransaction;
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
    private final CouponRepository couponRepository;

    public RedisResult execute(RedisVo redisVO) {
        List<Object> execute = redisTransaction.execute(redisTemplate, operation, redisVO);
        Object amount =  execute.get(0);
        if (amount == null) {
            amount = couponRepository.getCouponWithLock(redisVO.couponId());
        }

        return new RedisResult(Long.parseLong(String.valueOf(amount)), Long.parseLong(String.valueOf(execute.get(1))), Long.parseLong(String.valueOf(execute.get(2))) != 1L);
    }

    public Long getCount(Long couponId) {
        return redisTemplate.opsForValue()
                .decrement("couponId:" + couponId);
    }


    public Long checkDuplicate(Long couponId, String email) {
        return redisTemplate.opsForSet()
                .add("couponId:" + couponId + email, email);
    }
}
