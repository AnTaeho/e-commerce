package com.example.e_commerce.coupon.redis;

import org.springframework.data.redis.core.RedisOperations;

public interface RedisOperation<T> {

    Long count(RedisOperations<String, Object> operations, T t);

    Long add(RedisOperations<String, Object> operations, T t);

    void execute(RedisOperations callbackOperations, Object vo);
}
