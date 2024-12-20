package com.example.e_commerce.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.e_commerce.coupon.controller.dto.CouponRequest;
import com.example.e_commerce.coupon.controller.dto.CouponResponse;
import com.example.e_commerce.coupon.domain.Coupon;
import com.example.e_commerce.coupon.repository.CouponRepository;
import com.example.e_commerce.coupon.service.CouponService;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class CouponTest {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private Long couponId;

    @BeforeEach
    @Transactional
    void setUp() {
        CouponRequest couponRequest = new CouponRequest(1000, 5, LocalDate.of(2024, 12, 25));
        CouponResponse response = couponService.createCoupon(couponRequest);
        couponId = response.couponId();
        redisTemplate.opsForValue().set("couponId:" + couponId, "1000");
    }

    @AfterEach
    void afterEach() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("쿠폰 발급 성공")
    void IssueCouponTest() {
        // given
        CouponRequest couponRequest = new CouponRequest(100, 5, LocalDate.of(2024, 12, 25));
        CouponResponse response = couponService.createCoupon(couponRequest);
        redisTemplate.opsForValue().set("couponId:" + response.couponId(), "100");

        // when
        couponService.issueCouponWithPessimisticLock(response.couponId(), UUID.randomUUID().toString());


        // then
        Coupon coupon = couponRepository.findById(response.couponId()).orElseThrow();
        Assertions.assertThat(coupon.getAmount()).isEqualTo(99);
    }

    @Test
    @DisplayName("비관적 락 - 수량 10개의 쿠폰을 15명이 발급 시도하면 10명 성공하고 5명은 실패한다.")
    void issueCouponWithManyPeopleWithLock() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(2000);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 2000; i++) {
            executorService.submit(() -> {
                try {
                    couponService.issueCouponWithPessimisticLock(couponId, UUID.randomUUID().toString());
                    successCount.incrementAndGet();
                } catch (IllegalArgumentException e) {
//                    System.out.println(e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long stopTime = System.currentTimeMillis();
        System.out.println(stopTime - startTime + "ms");

        Coupon updateCoupon = couponRepository.findById(couponId).orElseThrow();
        Assertions.assertThat(successCount.get()).isEqualTo(1000);
        Assertions.assertThat(failCount.get()).isEqualTo(1000);
        assertEquals(0, updateCoupon.getAmount());
    }

    @Test
    @DisplayName("레디스 - 수량 10개의 쿠폰을 15명이 발급 시도하면 10명 성공하고 5명은 실패한다.")
    void issueCouponWithManyPeopleWithRedis() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(2000);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            executorService.submit(() -> {
                try {
                    couponService.issueCouponWithRedisStock(couponId, UUID.randomUUID().toString());
                    successCount.incrementAndGet();
                } catch (IllegalArgumentException e) {
//                    System.out.println(e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long stopTime = System.currentTimeMillis();
        System.out.println(stopTime - startTime + "ms");

        Thread.sleep(5000);

        Coupon updateCoupon = couponRepository.findById(couponId).orElseThrow();
        Assertions.assertThat(successCount.get()).isEqualTo(1000);
        Assertions.assertThat(failCount.get()).isEqualTo(1000);
        assertEquals(0, updateCoupon.getAmount());
    }

}
