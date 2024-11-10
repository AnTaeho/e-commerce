package com.example.e_commerce.queue.service;

import static com.example.e_commerce.global.QueueConst.FIVE;
import static com.example.e_commerce.global.QueueConst.MINUTE;
import static com.example.e_commerce.global.QueueConst.PROCESSING_QUEUE_SIZE;

import com.example.e_commerce.queue.controller.dto.QueueResponse;
import com.example.e_commerce.queue.repository.QueueRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueQueryServiceImpl implements QueueQueryService {
    public final QueueRedisRepository queueRedisRepository;

    public QueueResponse findQueueStatus(Long userId, String token) {
        if (queueRedisRepository.isInProcessingQueue(userId)) {
            return QueueResponse.processing();
        }

        Integer position = queueRedisRepository.getWaitingQueuePosition(userId, token);
        if (position != null) {
            return QueueResponse.waiting(position, calculateEstimatedWaitSeconds(position));
        } else {
            return QueueResponse.notInQueue();
        }
    }

    private Integer calculateEstimatedWaitSeconds(int position) {
        int batchSize = PROCESSING_QUEUE_SIZE;
        int batchInterval = FIVE * MINUTE;
        int batches = position / batchSize;
        return batches * batchInterval;
    }
}
