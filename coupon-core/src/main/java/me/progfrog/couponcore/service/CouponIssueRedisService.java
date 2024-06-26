package me.progfrog.couponcore.service;

import lombok.RequiredArgsConstructor;
import me.progfrog.couponcore.repository.redis.RedisRepository;
import org.springframework.stereotype.Service;

import static me.progfrog.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;

    public boolean isAvailableTotalIssueQuantity(Integer totalQuantity, long couponId) {
        if (totalQuantity == null) {
            // 무제한 발급할 수 있는 쿠폰의 경우
            return true;
        }

        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    public boolean isAvailableUserIssueQuantity(long couponId, long userId) {
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }
}
