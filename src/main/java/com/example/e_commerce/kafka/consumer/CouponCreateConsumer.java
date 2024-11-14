package com.example.e_commerce.kafka.consumer;

import com.example.e_commerce.coupon.domain.Coupon;
import com.example.e_commerce.coupon.domain.CouponOutbox;
import com.example.e_commerce.coupon.domain.UserCoupon;
import com.example.e_commerce.coupon.repository.CouponOutboxRepository;
import com.example.e_commerce.coupon.repository.CouponRepository;
import com.example.e_commerce.coupon.repository.UserCouponRepository;
import com.example.e_commerce.event.coupon.CouponEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponCreateConsumer {

    private final UserCouponRepository userCouponRepository;
    private final CouponOutboxRepository couponOutboxRepository;
    private final CouponRepository couponRepository;

    @Transactional
    @KafkaListener(topics = "issue-coupon", groupId = "group_1")
    public void listener(CouponEvent couponEvent) {
        userCouponRepository.save(new UserCoupon(couponEvent.couponId(), couponEvent.email()));
        decreaseTicketAmount(couponEvent);
        updateOutboxStatus(couponEvent);
    }

    private void decreaseTicketAmount(CouponEvent couponEvent) {
        Coupon coupon = couponRepository.findById(couponEvent.couponId())
                .orElseThrow(() -> new IllegalArgumentException("해당 티켓은 없습니다."));
        coupon.issue();
    }

    private void updateOutboxStatus(CouponEvent couponEvent) {
        CouponOutbox couponOutbox = couponOutboxRepository.findByCouponIdIdAndEmail(couponEvent.couponId(), couponEvent.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 기록을 찾을 수 없습니다."));
        couponOutbox.done();
    }

}
