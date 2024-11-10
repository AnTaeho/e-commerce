package com.example.e_commerce.coupon.repository;

import com.example.e_commerce.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("select c.amount from Coupon c where c.id = :couponId")
    Long getCouponAmount(@Param("couponId") Long couponId);
}
