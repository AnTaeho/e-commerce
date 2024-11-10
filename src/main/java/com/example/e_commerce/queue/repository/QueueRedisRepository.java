package com.example.e_commerce.queue.repository;

import static com.example.e_commerce.global.QueueConst.PROCESSING_QUEUE_KEY;
import static com.example.e_commerce.global.QueueConst.PROCESSING_QUEUE_SIZE;
import static com.example.e_commerce.global.QueueConst.PROCESSING_TOKEN_EXPIRATION_TIME;
import static com.example.e_commerce.global.QueueConst.WAITING_QUEUE_KEY;
import static com.example.e_commerce.global.QueueConst.WAITING_TOKEN_EXPIRATION_TIME;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository implements Serializable {

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSet;


    public void enqueueProcessingQueue(Long userId, String token) {
        String value = generateValue(userId, token);
        zSet.add(PROCESSING_QUEUE_KEY, value, System.currentTimeMillis() + PROCESSING_TOKEN_EXPIRATION_TIME);
    }

    public void enqueueWaitingQueue(Long userId, String token) {
        String value = generateValue(userId, token);
        zSet.add(WAITING_QUEUE_KEY, value, System.currentTimeMillis() + WAITING_TOKEN_EXPIRATION_TIME);
    }

    public boolean isProcessableNow() {
        Long pqSize = zSet.zCard(PROCESSING_QUEUE_KEY);
        Long wqSize = zSet.zCard(WAITING_QUEUE_KEY);
        if (pqSize == null) pqSize = 0L;
        if (wqSize == null) wqSize = 0L;

        return pqSize < PROCESSING_QUEUE_SIZE && wqSize == 0;
    }

    public boolean isInProcessingQueue(Long userId) {
        Set<String> values = zSet.range(PROCESSING_QUEUE_KEY, 0, -1);
        return values.stream().anyMatch(
                value -> value.matches(userId + ":.*")
        );
    }

    public boolean isInWaitingQueue(Long userId) {
        Set<String> values = zSet.range(WAITING_QUEUE_KEY, 0, -1);
        return values.stream().anyMatch(
                value -> value.matches(userId + ":.*")
        );
    }

    public Integer getWaitingQueuePosition(Long userId, String token) {
        String value = generateValue(userId, token);
        Long rank = zSet.rank(WAITING_QUEUE_KEY, value);
        return (rank == null) ? null : rank.intValue() + 1;
    }

    public Integer getProcessingQueueCount() {
        Long count = zSet.zCard(PROCESSING_QUEUE_KEY);
        return (count == null) ? 0 : count.intValue();
    }

    public List<String> getFrontTokensFromWaitingQueue(int count) {
        return new ArrayList<>(zSet.range(WAITING_QUEUE_KEY, 0, count - 1));
    }

    public void updateWaitingToProcessing(String value) {
        zSet.remove(WAITING_QUEUE_KEY, value);
        zSet.add(PROCESSING_QUEUE_KEY, value, System.currentTimeMillis() + PROCESSING_TOKEN_EXPIRATION_TIME);
    }

    public void removeTokenInWaitingQueue(Long userId, String token) {
        String value = generateValue(userId, token);
        zSet.remove(WAITING_QUEUE_KEY, value);
    }

    public void removeExpiredToken(Long currentTime) {
        zSet.removeRangeByScore(PROCESSING_QUEUE_KEY, Double.NEGATIVE_INFINITY, (double) currentTime);
        zSet.removeRangeByScore(WAITING_QUEUE_KEY, Double.NEGATIVE_INFINITY, (double) currentTime);
    }

    public void removeTokenInProcessingQueue(Long userId, String token) {
        String value = generateValue(userId, token);
        zSet.remove(PROCESSING_QUEUE_KEY, value);
    }

    private String generateValue(Long userId, String token) {
        return userId.toString() + ":" + token;
    }
}
