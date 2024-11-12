package com.example.e_commerce.coupon.service;

import com.example.e_commerce.coupon.controller.dto.CouponRequest;
import com.example.e_commerce.coupon.controller.dto.CouponResponse;
import com.example.e_commerce.coupon.controller.dto.RedisResult;
import com.example.e_commerce.coupon.controller.dto.RedisVo;
import com.example.e_commerce.coupon.domain.Coupon;
import com.example.e_commerce.coupon.domain.DiscountRate;
import com.example.e_commerce.coupon.repository.CouponRedisRepository;
import com.example.e_commerce.coupon.repository.CouponRepository;
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

    public CouponResponse createTicket(CouponRequest couponRequest) {
        Coupon coupon = new Coupon(
                couponRequest.amount(),
                DiscountRate.findBy(couponRequest.discount()),
                couponRequest.expiredAt()
        );
        Coupon savedCoupon = couponRepository.save(coupon);
        return new CouponResponse(savedCoupon.getId());
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

        publisher.publishEvent(new CouponEvent(couponId, email));
    }



}
