package com.example.e_commerce.coupon.repository;

import com.example.e_commerce.coupon.domain.CouponOutbox;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponOutboxRepository extends JpaRepository<CouponOutbox, Long> {

    @Query("select tob from CouponOutbox tob where tob.couponId = :couponId and tob.email = :email")
    Optional<CouponOutbox> findByCouponIdIdAndEmail(
            @Param("couponId") Long couponId,
            @Param("email") String email
    );
}
