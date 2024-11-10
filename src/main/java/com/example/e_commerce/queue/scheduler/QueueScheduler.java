package com.example.e_commerce.queue.scheduler;

import com.example.e_commerce.queue.service.QueueCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {
    private final QueueCommandService queueCommandService;

    @Scheduled(fixedRate = 30 * 1000) //30초마다
    public void updateWaitingToProcessing() {
        queueCommandService.removeExpiredToken();
        queueCommandService.updateWaitingToProcessing();
        log.info("processing queue 가 update 되었습니다.");
    }
}
