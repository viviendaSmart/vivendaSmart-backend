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

        // Límites solo para calcular el monto del AVN
        BigDecimal sizeLimitSmall = new BigDecimal("50");
        BigDecimal priceLimit1 = new BigDecimal("60000");
        BigDecimal priceLimit2 = new BigDecimal("70000");
        BigDecimal priceLimit3 = new BigDecimal("109000");
        BigDecimal priceLimit4 = new BigDecimal("136000");

        switch (type) {

            case "AVN":
                // OJO: asumimos que si llega "AVN" ya pasó las validaciones
                // de ingreso y tamaño máximo en el frontend.

                if (propertySize != null && propertySize.compareTo(sizeLimitSmall) <= 0) {
                    // Vivienda hasta 50 m²
                    return new BigDecimal("46545");
                } else {
                    // Tramos según precio de la vivienda
                    if (propertyPrice.compareTo(priceLimit1) <= 0) {
                        return new BigDecimal("56710");
                    } else if (propertyPrice.compareTo(priceLimit2) <= 0) {
                        return new BigDecimal("51895");
                    } else if (propertyPrice.compareTo(priceLimit3) <= 0) {
                        return new BigDecimal("50825");
                    } else if (propertyPrice.compareTo(priceLimit4) <= 0) {
                        return new BigDecimal("46545");
                    } else {
                        // Precio fuera del rango definido de Techo Propio
                        return BigDecimal.ZERO;
                    }
                }

            case "CSP":
                // Aquí asumimos que si llega "CSP", el frontend ya verificó income <= 2706
                return new BigDecimal("32100");

            case "MV":
                // Igual: si llega "MV", el frontend ya validó income <= 2706
                return new BigDecimal("12305");

            case "NONE":
                return BigDecimal.ZERO;

            default:
                return BigDecimal.ZERO;
        }
    }
}
