package com.firststudent.platform.viviendasmartbackend.simulator.domain.services;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BonusCalculator {

    public BigDecimal calculate(BigDecimal monthlyIncome,
                                BigDecimal propertySize,
                                BigDecimal propertyPrice,
                                String bonusType) {
        if (bonusType == null) return BigDecimal.ZERO;

        String type = bonusType.trim().toUpperCase();

        BigDecimal incomeAVNMax = new BigDecimal("3715");
        BigDecimal incomeCSP_MV_Max = new BigDecimal("2706");
        BigDecimal maxSizeAVN = new BigDecimal("140");
        BigDecimal sizeLimitSmall = new BigDecimal("50");
        BigDecimal priceLimit1 = new BigDecimal("60000");
        BigDecimal priceLimit2 = new BigDecimal("70000");
        BigDecimal priceLimit3 = new BigDecimal("109000");
        BigDecimal priceLimit4 = new BigDecimal("136000");

        switch (type) {
            case "AVN":
                if (monthlyIncome.compareTo(incomeAVNMax) >= 0) return BigDecimal.ZERO;
                if (propertySize.compareTo(maxSizeAVN) > 0) return BigDecimal.ZERO;

                if (propertySize.compareTo(sizeLimitSmall) <= 0) {
                    return new BigDecimal("46545");
                } else {
                    if (propertyPrice.compareTo(priceLimit1) <= 0) {
                        return new BigDecimal("56710");
                    } else if (propertyPrice.compareTo(priceLimit2) <= 0) {
                        return new BigDecimal("51895");
                    } else if (propertyPrice.compareTo(priceLimit3) <= 0) {
                        return new BigDecimal("50825");
                    } else if (propertyPrice.compareTo(priceLimit4) <= 0) {
                        return new BigDecimal("46545");
                    } else {
                        return BigDecimal.ZERO;
                    }
                }
            case "CSP":
                return monthlyIncome.compareTo(incomeCSP_MV_Max) < 0
                        ? new BigDecimal("32100")
                        : BigDecimal.ZERO;
            case "MV":
                return monthlyIncome.compareTo(incomeCSP_MV_Max) < 0
                        ? new BigDecimal("12305")
                        : BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }
}

