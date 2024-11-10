package com.example.e_commerce.coupon.redis;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisTransaction {

    public List<Object> execute(
            RedisTemplate<String, String> redisTemplate, RedisOperation operation, Object vo) {

        return redisTemplate.execute(
                new SessionCallback<>() {
                    @Override
                    public List<Object> execute(RedisOperations callbackOperations) throws DataAccessException {

                        // [1] REDIS 트랜잭션 Start
                        callbackOperations.multi();

                        // [2] Operation 실행
                        operation.execute(callbackOperations, vo);

                        // [3] REDIS 트랜잭션 End
                        return callbackOperations.exec();
                    }
                });
    }
}
