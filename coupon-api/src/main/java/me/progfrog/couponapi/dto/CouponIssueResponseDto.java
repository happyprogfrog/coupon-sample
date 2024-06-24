package me.progfrog.couponapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(value = NON_NULL)
public record CouponIssueResponseDto(
        boolean isSuccess,
        String code,
        String reason
) {
}
