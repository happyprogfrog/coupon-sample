package me.progfrog.couponcore.service;

import lombok.RequiredArgsConstructor;
import me.progfrog.couponcore.model.Coupon;
import me.progfrog.couponcore.repository.redis.RedisRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;

    public void issue(long couponId, long userId) {
        // 쿠폰 발급에 대한 검증을 진행하고, 검증 성공 시 쿠폰 발급처리까지 진행
        // 1. 유저의 요청을 sorted set 적재
        String key = "issue.request.sorted_set.couponId=%s".formatted(couponId);
        redisRepository.zAdd(key, String.valueOf(userId), System.currentTimeMillis());

        // 2. 유저의 요청 순서를 조회
        // 3. 조회 결과를 선착순 조건과 비교
        // 4. 쿠폰 발급 queue에 적재
    }
}
