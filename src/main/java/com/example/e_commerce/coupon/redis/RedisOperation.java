package com.example.e_commerce.coupon.redis;

import org.springframework.data.redis.core.RedisOperations;

public interface RedisOperation<T> {

    Long getAmount(RedisOperations<String, String> operations, T t);

    Long count(RedisOperations<String, String> operations, T t);

    Long add(RedisOperations<String, String> operations, T t);

    void execute(RedisOperations callbackOperations, Object vo);
}
