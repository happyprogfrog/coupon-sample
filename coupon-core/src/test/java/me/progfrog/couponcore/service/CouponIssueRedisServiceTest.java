package me.progfrog.couponcore.service;

import me.progfrog.couponcore.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.stream.IntStream;

import static me.progfrog.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.assertj.core.api.Assertions.assertThat;

class CouponIssueRedisServiceTest extends TestConfig {

    @Autowired
    CouponIssueRedisService couponIssueRedisService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void clear() {
        Collection<String> redisKey = redisTemplate.keys("*");
        redisTemplate.delete(redisKey);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하면 true 반환")
    void isAvailableTotalIssueQuantity_1() {
        // given
        int totalIssueQuantity = 10;
        long couponId = 1;

        // when
        boolean result = couponIssueRedisService.isAvailableTotalIssueQuantity(totalIssueQuantity, couponId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 모두 소진되면 false 반환")
    void isAvailableTotalIssueQuantity_2() {
        // given
        int totalIssueQuantity = 10;
        long couponId = 1;

        IntStream.range(0, totalIssueQuantity).forEach(userId -> {
            redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));
        });

        // when
        boolean result = couponIssueRedisService.isAvailableTotalIssueQuantity(totalIssueQuantity, couponId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하지 않으면 true 반환")
    void isAvailableUserIssueQuantity_1() {
        // given
        long couponId = 1;
        long userId = 1;

        // when
        boolean result = couponIssueRedisService.isAvailableUserIssueQuantity(couponId, userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하면 false 반환")
    void isAvailableUserIssueQuantity_2() {
        // given
        long couponId = 1;
        long userId = 1;
        redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));

        // when
        boolean result = couponIssueRedisService.isAvailableUserIssueQuantity(couponId, userId);

        // then
        assertThat(result).isFalse();
    }
}