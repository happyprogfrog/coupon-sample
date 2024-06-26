package me.progfrog.couponcore.repository.redis.dto;

public record CouponPushQueueRequest(
        long couponId,
        long userId
) {
}
