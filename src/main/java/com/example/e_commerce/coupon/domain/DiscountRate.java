package com.example.e_commerce.coupon.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum DiscountRate {
    FIVE(5),
    TEN(10),
    FIFTEEN(15),
    TWENTY(20);

    private final int value;

    DiscountRate(int value) {
        this.value = value;
    }

    public static DiscountRate findBy(final int discountRate) {
        return Arrays.stream(DiscountRate.values())
                .filter(it -> it.value == discountRate)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(""));
    }

    public String getStringForm() {
        return this.value + "%";
    }
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice) {
        BigDecimal hundred = new BigDecimal("100");
        BigDecimal discountValue = new BigDecimal(this.value);
        BigDecimal percentage = hundred.subtract(discountValue);

        BigDecimal discountedPercentage = percentage.divide(hundred, 1, RoundingMode.DOWN);
        return originalPrice.multiply(discountedPercentage).setScale(1, RoundingMode.DOWN);
    }
}
