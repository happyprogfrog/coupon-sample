package me.progfrog.couponcore.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponIssueException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, message);
    }
}
