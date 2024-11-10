package com.example.e_commerce.queue.service;

import static com.example.e_commerce.global.QueueConst.ALREADY_ISSUED_USER;
import static com.example.e_commerce.global.QueueConst.PROCESSING_QUEUE_SIZE;
import static com.example.e_commerce.global.QueueConst.REMOVE_BAD_REQUEST;

import com.example.e_commerce.queue.repository.QueueRedisRepository;
import com.example.e_commerce.queue.util.QueueJwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueCommandServiceImpl implements QueueCommandService {
    private final QueueRedisRepository queueRedisRepository;
    public final QueueJwtUtil queueJwtUtil;

    @Override
    public String issueTokenAndEnqueue(Long userId) {
        String token = queueJwtUtil.createJwt(userId);

        checkAlreadyIssuedUser(userId);

        if (queueRedisRepository.isProcessableNow()) {
            queueRedisRepository.enqueueProcessingQueue(userId, token);
        } else {
            queueRedisRepository.enqueueWaitingQueue(userId, token);
        }

        return token;
    }

    @Override
    public void updateWaitingToProcessing() {
        int count = calculateAvailableProcessingCount();
        if (count == 0) return;

        List<String> values = queueRedisRepository.getFrontTokensFromWaitingQueue(count);
        values.forEach(queueRedisRepository::updateWaitingToProcessing);
    }

    @Override
    public void removeExpiredToken() {
        queueRedisRepository.removeExpiredToken(System.currentTimeMillis());
    }

    @Override
    public void removeTokenInWaitingQueue(Long userId, String token) {
        Long tokenUserId = queueJwtUtil.getUserIdByToken(token);
        if(userId.equals(tokenUserId))
            queueRedisRepository.removeTokenInWaitingQueue(userId, token);
        else throw new IllegalArgumentException(REMOVE_BAD_REQUEST);
    }

    @Override
    public void removeTokenInProcessingQueue(Long userId, String token) {
        Long tokenUserId = queueJwtUtil.getUserIdByToken(token);
        if(userId.equals(tokenUserId))
            queueRedisRepository.removeTokenInProcessingQueue(userId, token);
        else throw new IllegalArgumentException(REMOVE_BAD_REQUEST);
    }

    private void checkAlreadyIssuedUser(Long userId) {
        if (queueRedisRepository.isInWaitingQueue(userId)
                || queueRedisRepository.isInProcessingQueue(userId)) {
            throw new IllegalArgumentException(ALREADY_ISSUED_USER);
        }
    }

    private int calculateAvailableProcessingCount() {
        int size = queueRedisRepository.getProcessingQueueCount();
        return PROCESSING_QUEUE_SIZE - size;
    }
}
