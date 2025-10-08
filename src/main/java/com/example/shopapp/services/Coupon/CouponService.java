package com.example.shopapp.services.Coupon;

import com.example.shopapp.models.Coupon;
import com.example.shopapp.models.CouponCondition;
import com.example.shopapp.repositories.CouponConditionRepository;
import com.example.shopapp.repositories.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService{

    private final CouponRepository couponRespository;
    private final CouponConditionRepository couponConditionRepository;

    @Override
    public double calculateCouponValue(String couponCode, double totalAmount) {
        Coupon coupon = couponRespository.findByCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }
        double discount = calculateDiscount(coupon, totalAmount);
        return totalAmount - discount;
    }

    private double calculateDiscount(Coupon coupon, double totalAmount) {
        List<CouponCondition> conditions = couponConditionRepository.findByCouponId(coupon.getId());
        double discount = 0.0;
        double updateTotalAmount = totalAmount;
        for (CouponCondition condition : conditions) {
            // EAV (Entity - Attribute - Value) - Mô Hình
            String attribute = condition.getAttribute();
            String operator = condition.getOperator();
            String value = condition.getValue();
            double percenDiscount = Double.parseDouble(
                    String.valueOf(condition.getDiscountAmount()));

            if (attribute.equals("minimum_amount")) {
                if (operator.equals(">") && updateTotalAmount > Double.parseDouble(value)) {
                    discount += updateTotalAmount * percenDiscount / 100;
                }
            } else if (attribute.equals("applicable_date")) {
                LocalDate applicableDate = LocalDate.parse(value);
                LocalDate currentDate = LocalDate.now();
                if (operator.equalsIgnoreCase("BETWEEN") && currentDate.isBefore(applicableDate)) {
                    discount += updateTotalAmount * percenDiscount / 100;
                }
            }

            // thêm nhiều điều kiện khác vào
            updateTotalAmount -= discount;
        }

        return discount;
    }
}
