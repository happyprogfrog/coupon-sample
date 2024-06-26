package me.progfrog.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.progfrog.couponcore.component.DistributeLockExecutor;
import me.progfrog.couponcore.model.Coupon;
import me.progfrog.couponcore.repository.redis.RedisRepository;
import me.progfrog.couponcore.repository.redis.dto.CouponPushQueueRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static me.progfrog.couponcore.exception.ErrorCode.*;
import static me.progfrog.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static me.progfrog.couponcore.util.CouponRedisUtils.getIssueRequestQueue;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(long couponId, long userId) {
        // 1. 쿠폰 존재 확인
        Coupon coupon = couponIssueService.findCoupon(couponId);

        distributeLockExecutor.execute("coupon_issue_lock_%s".formatted(couponId), 3000, 3000, () -> {
            // 2. 날짜 조회
            if (!coupon.isAvailableIssueDate()) {
                throw INVALID_COUPON_ISSUE_DATE.build(LocalDateTime.now(), coupon.getDateIssueStart(), coupon.getDateIssueEnd());
            }
            // 3. 수량 조회
            if (!couponIssueRedisService.isAvailableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
                throw INVALID_COUPON_ISSUE_QUANTITY.build();
            }

            // 4. 중복 발급 여부 확인
            if (!couponIssueRedisService.isAvailableUserIssueQuantity(couponId, userId)) {
                throw DUPLICATED_COUPON_ISSUE.build(couponId, userId);
            }

            // 5. 요청 추가 및 쿠폰 발급 큐에 적재
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
