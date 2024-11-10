package com.example.e_commerce.aop.lock;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSimpleLock {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisSimpleLock(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, String value, long leaseTime, TimeUnit timeUnit) {
        Boolean result = redisTemplate
                .opsForValue()
                .setIfAbsent(key, value, leaseTime, timeUnit);
        return result != null && result;
    }

    public boolean releaseLock(String key, String value) {
        var ops = redisTemplate.opsForValue();
        String lockValue = ops.get(key);

        if (value.equals(lockValue)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
