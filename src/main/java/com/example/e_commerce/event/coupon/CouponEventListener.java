package com.example.e_commerce.event.coupon;

import com.example.e_commerce.coupon.domain.CouponOutbox;
import com.example.e_commerce.coupon.repository.CouponOutboxRepository;
import com.example.e_commerce.kafka.producer.CouponMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponEventListener {

    private final CouponOutboxRepository couponOutboxRepository;
    private final CouponMessageProducer couponMessageProducer;

    @TransactionalEventListener(value = CouponEvent.class, phase = TransactionPhase.BEFORE_COMMIT)
    public void saveOutboxInfo(CouponEvent couponEvent) {
        couponOutboxRepository.save(new CouponOutbox(couponEvent.couponId(), couponEvent.email()));
    }

    @Async
    @TransactionalEventListener(value = CouponEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void produceMessage(CouponEvent couponEvent) {
        couponMessageProducer.create(couponEvent);
    }

}
