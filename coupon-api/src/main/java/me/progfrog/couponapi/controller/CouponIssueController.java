package me.progfrog.couponapi.controller;

import lombok.RequiredArgsConstructor;
import me.progfrog.couponapi.dto.CouponIssueRequestDto;
import me.progfrog.couponapi.service.CouponIssueRequestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponIssueController {

    private final CouponIssueRequestService couponIssueRequestService;

    @PostMapping("/api/v1/issue")
    public boolean issueV1(@RequestBody CouponIssueRequestDto requestDto) {
        couponIssueRequestService.issueRequestV1(requestDto);
        return true;
    }
}