package me.progfrog.couponapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.progfrog.couponapi.dto.CouponIssueRequestDto;
import me.progfrog.couponcore.service.CouponIssueService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;

    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        synchronized (this) {
            couponIssueService.issue(requestDto.couponId(), requestDto.userId());
        }
        log.info("쿠폰 발급 완료! coupon_id: %s, user_id: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }
}
