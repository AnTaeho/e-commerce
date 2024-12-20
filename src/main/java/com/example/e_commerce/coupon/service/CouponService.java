package com.example.e_commerce.coupon.service;

import com.example.e_commerce.coupon.controller.dto.CouponRequest;
import com.example.e_commerce.coupon.controller.dto.CouponResponse;
import com.example.e_commerce.coupon.controller.dto.RedisResult;
import com.example.e_commerce.coupon.controller.dto.RedisVo;
import com.example.e_commerce.coupon.domain.Coupon;
import com.example.e_commerce.coupon.domain.CouponOutbox;
import com.example.e_commerce.coupon.domain.DiscountRate;
import com.example.e_commerce.coupon.domain.UserCoupon;
import com.example.e_commerce.coupon.repository.CouponOutboxRepository;
import com.example.e_commerce.coupon.repository.CouponRedisRepository;
import com.example.e_commerce.coupon.repository.CouponRepository;
import com.example.e_commerce.coupon.repository.UserCouponRepository;
import com.example.e_commerce.event.coupon.CouponEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedisRepository couponRedisRepository;
    private final ApplicationEventPublisher publisher;

    private final UserCouponRepository userCouponRepository;
    private final CouponOutboxRepository couponOutboxRepository;

    @Transactional
    public CouponResponse createCoupon(CouponRequest couponRequest) {
        Coupon coupon = new Coupon(
                couponRequest.amount(),
                DiscountRate.findBy(couponRequest.discount()),
                couponRequest.expiredAt()
        );
        Coupon savedCoupon = couponRepository.save(coupon);
        return new CouponResponse(savedCoupon.getId());
    }

    @Transactional
    public void issueCouponWithPessimisticLock(Long couponId, String email) {
        Coupon coupon = couponRepository.getCouponWithLock(couponId).orElseThrow();
        if (coupon.getAmount() <= 0) {
            throw new IllegalArgumentException("수량 부족");
        }

        if (userCouponRepository.existsByEmailAndCouponId(email, couponId)) {
            throw new IllegalArgumentException("중복발급");
        }

        coupon.issue();
        userCouponRepository.save(new UserCoupon(couponId, email));
        couponRepository.saveAndFlush(coupon);
    }

    @Transactional
    public void issueCouponWithRedisStock(Long couponId, String email) {

        Long count = couponRedisRepository.getCount(couponId);
        if (count < 0L) {
            throw new IllegalArgumentException("수량 부족");
        }

        Long isIssued = couponRedisRepository.checkDuplicate(couponId, email);

        if (isIssued != 1L) {
            throw new IllegalArgumentException("중복 발급");
        }

        publisher.publishEvent(new CouponEvent(couponId, email));
    }

    @Transactional
    public void issueCouponWithRedisTransaction(Long couponId, String email) {

        RedisResult result = couponRedisRepository.execute(new RedisVo("coupon:" + couponId, email, couponId));

        if (result.size() >= result.amount()) {
            return;
        }

        if (result.alreadyIn()) {
            return;
        }

        userCouponRepository.save(new UserCoupon(couponId, email));
        decreaseTicketAmount(couponId);
    }

    private void decreaseTicketAmount(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("해당 티켓은 없습니다."));
        coupon.issue();
    }

    private void updateOutboxStatus(CouponEvent couponEvent) {
        CouponOutbox couponOutbox = couponOutboxRepository.findByCouponIdIdAndEmail(couponEvent.couponId(), couponEvent.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 기록을 찾을 수 없습니다."));
        couponOutbox.done();
    }



}
