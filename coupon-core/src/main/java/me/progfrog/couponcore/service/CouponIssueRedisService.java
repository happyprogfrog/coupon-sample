package me.progfrog.couponcore.service;

import lombok.RequiredArgsConstructor;
import me.progfrog.couponcore.repository.redis.RedisRepository;
import me.progfrog.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.stereotype.Service;

import static me.progfrog.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static me.progfrog.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static me.progfrog.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;

    public void checkCouponIssueQuantity(CouponRedisEntity coupon, long userId) {
        if (!isAvailableTotalIssueQuantity(coupon.totalQuantity(), coupon.id())) {
            throw INVALID_COUPON_ISSUE_QUANTITY.build(coupon.totalQuantity(), getIssuedQuantity(coupon.id()));
        }
        if (!isAvailableUserIssueQuantity(coupon.id(), userId)) {
            throw DUPLICATED_COUPON_ISSUE.build(coupon.id(), userId);
        }
    }

    public boolean isAvailableTotalIssueQuantity(Integer totalQuantity, long couponId) {
        if (totalQuantity == null) {
            // 무제한 발급할 수 있는 쿠폰의 경우
            return true;
        }

        return totalQuantity > getIssuedQuantity(couponId);
    }

    public boolean isAvailableUserIssueQuantity(long couponId, long userId) {
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }

    private Long getIssuedQuantity(long couponId)
    {
        String key = getIssueRequestKey(couponId);
        return redisRepository.sCard(key);
    }
}
