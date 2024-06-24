package me.progfrog.couponapi.exception;

import me.progfrog.couponapi.dto.CouponIssueResponseDto;
import me.progfrog.couponcore.exception.CouponIssueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CouponAdviceController {

    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponseDto couponIssueExceptionHandler(CouponIssueException ex) {
        return new CouponIssueResponseDto(false, ex.getCode(), ex.getReason());
    }
}
