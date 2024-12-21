package com.example.e_commerce.aop.lock;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedisSimpleLock redisSimpleLock;

    @Around("@annotation(com.example.e_commerce.aop.lock.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String lockKey = distributedLock.key();
        String lockValue = UUID.randomUUID().toString();

        try {
            boolean acquired = redisSimpleLock.tryLock(
                    lockKey,
                    lockValue,
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );
            if (acquired) {
                throw new IllegalArgumentException("락을 얻을 수 없습니다.");
            }
            return joinPoint.proceed();
        } finally {
            redisSimpleLock.releaseLock(lockKey, lockValue);
        }
    }
}
