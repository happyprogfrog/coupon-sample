package me.progfrog.couponcore.service;

import me.progfrog.couponcore.TestConfig;
import me.progfrog.couponcore.exception.CouponIssueException;
import me.progfrog.couponcore.model.Coupon;
import me.progfrog.couponcore.model.CouponIssue;
import me.progfrog.couponcore.model.CouponType;
import me.progfrog.couponcore.repository.mysql.CouponIssueJpaRepository;
import me.progfrog.couponcore.repository.mysql.CouponIssueRepository;
import me.progfrog.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static me.progfrog.couponcore.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CouponIssueServiceTest extends TestConfig {

    @Autowired
    CouponIssueService couponIssueService;

    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @BeforeEach
    void clean() {
        couponJpaRepository.deleteAllInBatch();
        couponIssueJpaRepository.deleteAllInBatch();;
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 있으면, 예외를 반환")
    void saveCouponIssue_1() {
        // given
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();
        couponIssueJpaRepository.save(couponIssue);

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () ->
                couponIssueService.saveCouponIssue(couponIssue.getCouponId(), couponIssue.getUserId()));
        assertThat(ex.getErrorCode()).isEqualTo(DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 없으면, 발급 성공")
    void saveCouponIssue_2() {
        // given
        long couponId = 1L;
        long userId = 1L;

        // when
        CouponIssue result = couponIssueService.saveCouponIssue(couponId, userId);

        // then
        assertThat(couponIssueJpaRepository.findById(result.getId())).isPresent();
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제가 없다면 쿠폰 발급")
    void issue_1() {
        // given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);

        // when
        couponIssueService.issue(coupon.getId(), userId);

        // then
        Coupon couponResult = couponJpaRepository.findById(coupon.getId()).get();
        assertThat(couponResult.getIssuedQuantity()).isEqualTo(1);

        CouponIssue couponIssueResult = couponIssueRepository.findFirstCouponIssue(coupon.getId(), userId);
        assertThat(couponIssueResult).isNotNull();
    }

    @Test
    @DisplayName("발급 수량에 문제가 있다면 예외 발생")
    void issue_2() {
        // given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () ->
                        couponIssueService.issue(coupon.getId(), userId));
        assertThat(ex.getErrorCode()).isEqualTo(INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("기한에 문제가 있다면 예외 발생")
    void issue_3() {
        // given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () ->
                        couponIssueService.issue(coupon.getId(), userId));
        assertThat(ex.getErrorCode()).isEqualTo(INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("중복 발급에 문제가 있다면 예외 발생")
    void issue_4() {
        // given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);

        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .build();
        couponIssueJpaRepository.save(couponIssue);

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () ->
                        couponIssueService.issue(coupon.getId(), userId));
        assertThat(ex.getErrorCode()).isEqualTo(DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않는다면 예외 발생")
    void issue_5() {
        // given
        long userId = 1L;
        long couponId = 1L;

        // when & then
        CouponIssueException ex = assertThrows(CouponIssueException.class, () ->
                couponIssueService.issue(couponId, userId));
        assertThat(ex.getErrorCode()).isEqualTo(COUPON_NOT_EXIST);
    }
}