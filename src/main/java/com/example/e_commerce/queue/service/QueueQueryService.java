package com.example.e_commerce.queue.service;

import com.example.e_commerce.queue.controller.dto.QueueResponse;

public interface QueueQueryService {

    QueueResponse findQueueStatus(Long userId, String token);

}
