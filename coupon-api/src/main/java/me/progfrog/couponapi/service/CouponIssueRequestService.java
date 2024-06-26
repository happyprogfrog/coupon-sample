package me.progfrog.couponapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.progfrog.couponapi.dto.CouponIssueRequestDto;
import me.progfrog.couponcore.service.AsyncCouponIssueServiceV1;
import me.progfrog.couponcore.service.AsyncCouponIssueServiceV2;
import me.progfrog.couponcore.service.CouponIssueService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;
    private final AsyncCouponIssueServiceV1 asyncCouponIssueServiceV1;
    private final AsyncCouponIssueServiceV2 asyncCouponIssueServiceV2;

    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        couponIssueService.issue(requestDto.couponId(), requestDto.userId());
        log.info("쿠폰 발급 완료! coupon_id: %s, user_id: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }

    public void asyncIssueRequestV1(CouponIssueRequestDto requestDto) {
        asyncCouponIssueServiceV1.issue(requestDto.couponId(), requestDto.userId());
        log.info("비동기 버전 1 쿠폰 발급 완료! coupon_id: %s, user_id: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }

    public void asyncIssueRequestV2(CouponIssueRequestDto requestDto) {
        asyncCouponIssueServiceV2.issue(requestDto.couponId(), requestDto.userId());
        log.info("비동기 버전 2 쿠폰 발급 완료! coupon_id: %s, user_id: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }
}
