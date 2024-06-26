package me.progfrog.counponconsumer.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.progfrog.couponcore.repository.redis.RedisRepository;
import me.progfrog.couponcore.repository.redis.dto.CouponPushQueueRequest;
import me.progfrog.couponcore.service.CouponIssueService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static me.progfrog.couponcore.util.CouponRedisUtils.getIssueRequestQueue;

@RequiredArgsConstructor
@EnableScheduling
@Component
@Slf4j
public class CouponIssueListener {

    private final RedisRepository redisRepository;
    private final CouponIssueService couponIssueService;

    private final String issueRequestQueueKey = getIssueRequestQueue();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 1000L)
    public void issue() throws JsonProcessingException {
        log.info("Listen...");

        while (isExistCouponIssueTarget()) {
            CouponPushQueueRequest target = getIssueTarget();
            log.info("발급 시작되었습니다. target: %s".formatted(target));
            couponIssueService.issue(target.couponId(), target.userId());
            log.info("발급이 완료되었습니다. target: %s".formatted(target));
            removeIssuedTarget();
        }
    }

    private boolean isExistCouponIssueTarget() {
        return redisRepository.lSize(issueRequestQueueKey) > 0;
    }

    private CouponPushQueueRequest getIssueTarget() throws JsonProcessingException {
        return objectMapper.readValue(redisRepository.lIndex(issueRequestQueueKey, 0), CouponPushQueueRequest.class);
    }

    private void removeIssuedTarget() {
        redisRepository.lPop(issueRequestQueueKey);
    }
}
