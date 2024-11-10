package com.example.e_commerce.queue.service.facade;

import com.example.e_commerce.queue.controller.dto.QueueResponse;
import com.example.e_commerce.queue.controller.dto.TokenResponse;

public interface QueueService {
    TokenResponse issueTokenAndEnqueue(Long userId);
    void dequeueWaitingQueue(Long userId, String token);
    void dequeueProcessingQueue(Long userId, String token);
    QueueResponse findQueueStatus(Long userId, String token);
}
