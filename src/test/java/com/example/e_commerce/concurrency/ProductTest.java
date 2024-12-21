package com.example.e_commerce.concurrency;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.e_commerce.coupon.domain.Coupon;
import com.example.e_commerce.history.domain.History;
import com.example.e_commerce.history.repository.HistoryRepository;
import com.example.e_commerce.product.controller.dto.BuyRequest;
import com.example.e_commerce.product.controller.dto.ProductRegisterRequest;
import com.example.e_commerce.product.controller.dto.ProductResponse;
import com.example.e_commerce.product.domain.Product;
import com.example.e_commerce.product.repository.ProductRepository;
import com.example.e_commerce.product.service.ProductService;
import com.example.e_commerce.user.domain.User;
import com.example.e_commerce.user.domain.UserRole;
import com.example.e_commerce.user.repository.UserRepository;
import java.util.List;
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

@SpringBootTest
public class ProductTest {

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HistoryRepository historyRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private User savedUser;
    private Long productId;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.user("name", "email", "password"));
        ProductRegisterRequest request = new ProductRegisterRequest("name", 1, 10);
        ProductResponse response = productService.registerProduct(request);
        productId = response.productId();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        userRepository.deleteAll();
        historyRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("물품 구매 성공 테스트")
    void buyProductTest() {
        // given
//        ProductRegisterRequest request = new ProductRegisterRequest("name", 1, 10);
//        ProductResponse response = productService.registerProduct(request);

        // when
        BuyRequest buyRequest = new BuyRequest(productId, 5);
        productService.sell(buyRequest, savedUser.getEmail());

        // then
        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getAmount()).isEqualTo(5);
    }

    @Test
    @DisplayName("레디스 - 수량 10개의 쿠폰을 15명이 발급 시도하면 10명 성공하고 5명은 실패한다.")
    void issueCouponWithManyPeopleWithRedis() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(15);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 15; i++) {
            executorService.submit(() -> {
                try {
                    productService.sell(new BuyRequest(productId, 1), savedUser.getEmail());
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

        Product product = productRepository.findById(productId).orElseThrow();
        List<History> all = historyRepository.findAll();
        Assertions.assertThat(all).hasSize(10);

        Assertions.assertThat(successCount.get()).isEqualTo(10);
        Assertions.assertThat(failCount.get()).isEqualTo(5);
        assertEquals(0, product.getAmount());
    }

}
