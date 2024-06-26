package me.progfrog.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.progfrog.couponcore.component.DistributeLockExecutor;
import me.progfrog.couponcore.repository.redis.RedisRepository;
import me.progfrog.couponcore.repository.redis.dto.CouponPushQueueRequest;
import me.progfrog.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.stereotype.Service;

import static me.progfrog.couponcore.exception.ErrorCode.FAIL_COUPON_PUSH_QUEUE_REQUEST;
import static me.progfrog.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static me.progfrog.couponcore.util.CouponRedisUtils.getIssueRequestQueue;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponCacheService couponCacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(long couponId, long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();
        distributeLockExecutor.execute("coupon_issue_lock_%s".formatted(couponId), 3000, 3000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });
    }

    private void issueRequest(long couponId, long userId) {
        CouponPushQueueRequest pushQueueRequest = new CouponPushQueueRequest(couponId, userId);
        try {
            // 요청 추가
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            // 쿠폰 발급 큐에 적재
            String value = objectMapper.writeValueAsString(pushQueueRequest);
            redisRepository.rPush(getIssueRequestQueue(), value);
        } catch (JsonProcessingException e) {
            throw FAIL_COUPON_PUSH_QUEUE_REQUEST.build(pushQueueRequest);
        }
    }
}
