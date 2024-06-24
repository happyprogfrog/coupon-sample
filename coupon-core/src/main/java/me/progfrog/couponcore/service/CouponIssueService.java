package me.progfrog.couponcore.service;

import lombok.RequiredArgsConstructor;
import me.progfrog.couponcore.exception.CouponIssueException;
import me.progfrog.couponcore.model.Coupon;
import me.progfrog.couponcore.model.CouponIssue;
import me.progfrog.couponcore.repository.mysql.CouponIssueJpaRepository;
import me.progfrog.couponcore.repository.mysql.CouponIssueRepository;
import me.progfrog.couponcore.repository.mysql.CouponJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static me.progfrog.couponcore.exception.ErrorCode.COUPON_NOT_EXIST;
import static me.progfrog.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;

@RequiredArgsConstructor
@Service
public class CouponIssueService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;
    private final CouponIssueRepository couponIssueRepository;

    @Transactional
    public void issue(long couponId, long userId) {
        // 쿠폰 발급에 대한 검증을 진행하고, 검증 성공 시 쿠폰 발급처리까지 진행
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
    }

   @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId) {
        return couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CouponIssueException(COUPON_NOT_EXIST,
                        "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId)));
    }

    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        return couponIssueJpaRepository.save(couponIssue);
    }

    private void checkAlreadyIssuance(long couponId, long userId) {
        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(couponId, userId);
        if (issue != null) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE,
                    "이미 발급된 쿠폰입니다. coupon_id: %s, user_id: %s".formatted(userId, couponId));
        }
    }
}
