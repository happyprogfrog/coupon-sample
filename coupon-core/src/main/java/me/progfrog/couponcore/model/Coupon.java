package me.progfrog.couponcore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static me.progfrog.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static me.progfrog.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart;

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd;

    public boolean isAvailableIssueQuantity() {
        if (totalQuantity == null) {
            // 무제한 발급할 수 있는 쿠폰의 경우
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean isAvailableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueEnd.isBefore(now) || !isAvailableIssueQuantity();
    }

    public void issue() {
        if (!isAvailableIssueQuantity()) {
            throw INVALID_COUPON_ISSUE_QUANTITY.build(totalQuantity, issuedQuantity);
        }
        if (!isAvailableIssueDate()) {
            throw INVALID_COUPON_ISSUE_DATE.build(LocalDateTime.now(), dateIssueStart, dateIssueEnd);
        }
        issuedQuantity++;
    }
}
