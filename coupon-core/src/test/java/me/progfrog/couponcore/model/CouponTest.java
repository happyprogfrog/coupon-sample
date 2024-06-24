package me.progfrog.couponcore.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @Test
    @DisplayName("발급 수량이 남아있다면 true를 반환")
    void isAvailableIssueQuantity_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();

        // when
        boolean result = coupon.isAvailableIssueQuantity();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("발급 수량이 모두 소진되었다면 false 반환")
    void isAvailableIssueQuantity_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();

        // when
        boolean result = coupon.isAvailableIssueQuantity();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("최대 발급 수량이 설정되지 않았다면 항상 true 반환")
    void isAvailableIssueQuantity_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(100)
                .build();

        // when
        boolean result = coupon.isAvailableIssueQuantity();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("발급 기간이 시작되지 않았다면 false를 반환")
    void isAvailableIssueDate_1() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        boolean result = coupon.isAvailableIssueDate();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("발급 기간에 해당되면 true를 반환")
    void isAvailableIssueDate_2() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        boolean result = coupon.isAvailableIssueDate();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("발급 기간이 종료되면 false를 반환")
    void isAvailableIssueDate_3() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        // when
        boolean result = coupon.isAvailableIssueDate();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("발급 수량과 발급 기간이 모두 유효하면 쿠폰 발급에 성공")
    void issue_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        coupon.issue();

        // then
        assertEquals(coupon.getIssuedQuantity(), 100);
    }

    @Test
    @DisplayName("발급 수량이 초과하면 예외를 반환")
    void issue_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when & then
        assertThrows(RuntimeException.class, coupon::issue);
    }

    @Test
    @DisplayName("발급 기간이 유효하지 않으면 예외를 반환")
    void issue_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        // when & then
        assertThrows(RuntimeException.class, coupon::issue);
    }
}