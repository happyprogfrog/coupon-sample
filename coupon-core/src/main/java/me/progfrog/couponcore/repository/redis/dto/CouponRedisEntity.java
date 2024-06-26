package me.progfrog.couponcore.repository.redis.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import me.progfrog.couponcore.model.Coupon;
import me.progfrog.couponcore.model.CouponType;

import java.time.LocalDateTime;

import static me.progfrog.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static me.progfrog.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

public record CouponRedisEntity(
        Long id,
        CouponType couponType,
        Integer totalQuantity,
        boolean availableIssueQuantity,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueStart,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueEnd
) {

    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.isAvailableIssueQuantity(),
                coupon.getDateIssueStart(),
                coupon.getDateIssueEnd()
        );
    }

    private boolean isAvailableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    public void checkIssuableCoupon() {
        if (!availableIssueQuantity) {
            throw INVALID_COUPON_ISSUE_QUANTITY.build();
        }
        if (!isAvailableIssueDate()) {
            throw INVALID_COUPON_ISSUE_DATE.build(LocalDateTime.now(), dateIssueStart, dateIssueStart);
        }
    }
}
