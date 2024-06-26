package me.progfrog.couponcore.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_COUPON_ISSUE_QUANTITY("E-0001", "발급 가능 수량을 초과합니다. total: %s, issued: %s", "수량 초과"),
    INVALID_COUPON_ISSUE_DATE("E-0002", "발급 가능한 일자가 아닙니다. request: %s, issueStart: %s, issueEnd: %s", "일자 오류"),
    COUPON_NOT_EXIST("E-0003", "쿠폰 정책이 존재하지 않습니다. coupon_id: %s", "쿠폰 없음"),
    DUPLICATED_COUPON_ISSUE("E-0004", "이미 발급된 쿠폰입니다. coupon_id: %s, user_id: %s", "중복 발급"),
    FAIL_COUPON_PUSH_QUEUE_REQUEST("E-0005", "쿠폰 발급 요청에 실패하였습니다. input: %s", "요청 실패");

    private final String code;
    private final String detailReason;
    private final String simpleReason;

    public CouponIssueException build() {
        return new CouponIssueException(this, code, simpleReason);
    }

    public CouponIssueException build(Object ...args) {
        return new CouponIssueException(this, code, detailReason.formatted(args));
    }
}
