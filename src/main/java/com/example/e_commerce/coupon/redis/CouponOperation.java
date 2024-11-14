package com.example.e_commerce.coupon.redis;

import com.example.e_commerce.coupon.controller.dto.RedisVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponOperation implements RedisOperation<RedisVo> {

    @Override
    public Long getAmount(RedisOperations<String, String> operations, RedisVo redisVo) {
        String result = operations.opsForValue()
                .get(redisVo.key()+":amount");
        if (result == null) {
            return null;
        }
        return Long.parseLong(result);
    }

    @Override
    public Long count(RedisOperations<String, String> operations, RedisVo redisVO) {
        return operations.opsForSet()
                .size(redisVO.key());
    }

    @Override
    public Long add(RedisOperations<String, String> operations, RedisVo redisVO) {
        return operations.opsForSet()
                .add(redisVO.key(), redisVO.value());
    }

    @Override
    public void execute(RedisOperations callbackOperations, Object vo) {
        this.getAmount(callbackOperations, (RedisVo) vo);
        this.count(callbackOperations, (RedisVo) vo);
        this.add(callbackOperations, (RedisVo) vo);
    }
}
