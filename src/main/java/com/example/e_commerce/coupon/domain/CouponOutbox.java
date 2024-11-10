package com.example.e_commerce.coupon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponOutbox {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_outbox_id")
    private Long id;

    private Long couponId;
    private String email;

    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;

    public CouponOutbox(Long couponId, String email) {
        this.couponId = couponId;
        this.email = email;
        this.outboxStatus = OutboxStatus.CREATED;
    }

    public void done() {
        this.outboxStatus = OutboxStatus.DONE;
    }
}
