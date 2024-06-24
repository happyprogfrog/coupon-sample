package me.progfrog.couponcore.repository.mysql;

import me.progfrog.couponcore.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
