package com.example.e_commerce.coupon.redis;

import com.example.e_commerce.coupon.controller.dto.RedisVo;
import com.example.e_commerce.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponOperation implements RedisOperation<RedisVo> {

    private final CouponRepository couponRepository;

    @Override
    public Long getAmount(RedisOperations<String, Object> operations, RedisVo redisVo) {
        String result = (String) operations.opsForValue()
                .get(redisVo.key());
        if (result == null) {
            return getAmount(redisVo.couponId());
        }
        return Long.parseLong(result);
    }

    @Override
    public Long count(RedisOperations<String, Object> operations, RedisVo redisVO) {
        return operations.opsForSet()
                .size(redisVO.key());
    }

    @Override
    public Long add(RedisOperations<String, Object> operations, RedisVo redisVO) {
        return operations.opsForSet()
                .add(redisVO.key(), redisVO.value());
    }

    @Override
    public void execute(RedisOperations callbackOperations, Object vo) {
        this.getAmount(callbackOperations, (RedisVo) vo);
        this.count(callbackOperations, (RedisVo) vo);
        this.add(callbackOperations, (RedisVo) vo);
    }

    private Long getAmount(Long couponId) {
        return couponRepository.getCouponAmount(couponId);
    }
}
