package me.progfrog.couponcore.util;

public class CouponRedisUtils {

    public static String getIssueRequestKey(long couponId) {
        return "issue:request:couponId:%s".formatted(couponId);
    }

    public static String getIssueRequestQueue() {
        return "issue:request";
    }
}
