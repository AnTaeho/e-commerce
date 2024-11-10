package com.example.e_commerce.coupon.redis;

import com.example.e_commerce.coupon.controller.dto.RedisVo;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;

@Component
public class TicketOperation implements RedisOperation<RedisVo> {

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
        this.count(callbackOperations, (RedisVo) vo);
        this.add(callbackOperations, (RedisVo) vo);
    }

}
