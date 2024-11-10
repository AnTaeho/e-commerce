package com.example.e_commerce.kafka.producer;

import com.example.e_commerce.event.coupon.CouponEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponMessageProducer {

    private final KafkaTemplate<String, CouponEvent> kafkaTemplate;

    public void create(CouponEvent couponEvent) {
        kafkaTemplate.send("issue-coupon", couponEvent);
    }
}
