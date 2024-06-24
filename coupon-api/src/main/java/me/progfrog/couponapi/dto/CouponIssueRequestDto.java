package me.progfrog.couponapi.dto;

public record CouponIssueRequestDto(
        long couponId,
        long userId
) {
}
