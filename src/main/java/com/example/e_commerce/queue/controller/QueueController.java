package com.example.e_commerce.queue.controller;

import com.example.e_commerce.queue.controller.dto.QueueResponse;
import com.example.e_commerce.queue.controller.dto.TokenResponse;
import com.example.e_commerce.queue.service.facade.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/token/{userId}")
    public ResponseEntity<TokenResponse> issueTokenAndEnqueue(@PathVariable("userId") Long userId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(queueService.issueTokenAndEnqueue(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<QueueResponse> getQueueInfoWithToken(@PathVariable("userId") Long userId,
                                                               @RequestParam("token") String token)  {
        return ResponseEntity.ok()
                .body(queueService.findQueueStatus(userId, token));
    }

    @PostMapping("/wait/quit/{userId}")
    public ResponseEntity<Void> dequeueWaitingQueue(@PathVariable("userId") Long userId,
                                                    @RequestParam("token") String token) {
        queueService.dequeueWaitingQueue(userId, token);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/process/quit/{userId}")
    public ResponseEntity<Void> dequeueProcessingQueue(@PathVariable("userId") Long userId,
                                                       @RequestParam("token") String token) {
        queueService.dequeueProcessingQueue(userId, token);
        return ResponseEntity.ok(null);
    }
}
