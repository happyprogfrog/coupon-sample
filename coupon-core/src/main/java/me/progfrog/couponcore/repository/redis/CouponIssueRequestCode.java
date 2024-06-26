package me.progfrog.couponcore.repository.redis;

import me.progfrog.couponcore.exception.ErrorCode;

public enum CouponIssueRequestCode {
    SUCCESS(1),
    DUPLICATED_COUPON_ISSUE(2),
    INVALID_COUPON_ISSUE_QUANTITY(3);

    CouponIssueRequestCode(int code) {
    }

    public static CouponIssueRequestCode find(String code) {
        int codeValue = Integer.parseInt(code);
        return switch (codeValue) {
            case 1 -> SUCCESS;
            case 2 -> DUPLICATED_COUPON_ISSUE;
            case 3 -> INVALID_COUPON_ISSUE_QUANTITY;
            default -> throw new IllegalArgumentException("존재하지 않는 코드입니다. %s".formatted(code));
        };
    }

    public static void checkRequestResult(CouponIssueRequestCode code) {
        switch (code) {
            case INVALID_COUPON_ISSUE_QUANTITY -> throw ErrorCode.INVALID_COUPON_ISSUE_QUANTITY.build();
            case DUPLICATED_COUPON_ISSUE -> throw ErrorCode.DUPLICATED_COUPON_ISSUE.build();
        }
    }
}
