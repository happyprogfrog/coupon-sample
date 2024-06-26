package me.progfrog.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.progfrog.couponcore.TestConfig;
import me.progfrog.couponcore.exception.CouponIssueException;
import me.progfrog.couponcore.model.Coupon;
import me.progfrog.couponcore.model.CouponType;
import me.progfrog.couponcore.repository.mysql.CouponJpaRepository;
import me.progfrog.couponcore.repository.redis.dto.CouponPushQueueRequest;
import me.progfrog.couponcore.util.CouponRedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.IntStream;

import static me.progfrog.couponcore.exception.ErrorCode.*;
import static me.progfrog.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AsyncCouponIssueServiceV2Test extends TestConfig {


    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Autowired
    AsyncCouponIssueServiceV2 asyncCouponIssueServiceV2;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void clear() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 발급 - 쿠폰이 존재하지 않는다면 예외 반환")
    void issue_1() {
        // given
        long couponId = 1;
        long userId = 1;

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueServiceV2.issue(couponId, userId);
        });

        assertThat(ex.getErrorCode()).isEqualTo(COUPON_NOT_EXIST);
    }

    @Test
    @DisplayName("쿠폰 발급 - 날짜가 맞지 않는다면 예외 반환")
    void issue_2() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueServiceV2.issue(coupon.getId(), userId);
        });

        assertThat(ex.getErrorCode()).isEqualTo(INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("쿠폰 발급 - 발급 수량에 문제가 있다면 예외 반환")
    void issue_3() {
        // given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);

        IntStream.range(0, coupon.getTotalQuantity()).forEach(userId -> {
            redisTemplate.opsForSet().add(CouponRedisUtils.getIssueRequestKey(coupon.getId()), String.valueOf(userId));
        });

        long userId = 1000;

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueServiceV2.issue(coupon.getId(), userId);
        });

        assertThat(ex.getErrorCode()).isEqualTo(INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("쿠폰 발급 - 중복 발급 이라면 예외 반환")
    void issue_4() {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);

        redisTemplate.opsForSet().add(CouponRedisUtils.getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueServiceV2.issue(coupon.getId(), userId);
        });

        assertThat(ex.getErrorCode()).isEqualTo(DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 발급 - 검증을 통과하면 쿠폰 요청 성공")
    void issue_5() throws JsonProcessingException {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);

        // when
        asyncCouponIssueServiceV2.issue(coupon.getId(), userId);

        // then
        Boolean result1 = redisTemplate.opsForSet().isMember(CouponRedisUtils.getIssueRequestKey(coupon.getId()), String.valueOf(userId));
        assertThat(result1).isTrue();
    }

    @Test
    @DisplayName("쿠폰 발급 - 검증을 통과하면 쿠폰 발급 큐에 적재")
    void issue_6() throws JsonProcessingException {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);

        CouponPushQueueRequest queueRequest = new CouponPushQueueRequest(coupon.getId(), userId);
        String value = objectMapper.writeValueAsString(queueRequest);

        // when
        asyncCouponIssueServiceV2.issue(coupon.getId(), userId);

        // then
        String savedPushQueueRequest = redisTemplate.opsForList().leftPop(CouponRedisUtils.getIssueRequestQueue());
        assertThat(savedPushQueueRequest).isEqualTo(value);
    }
}