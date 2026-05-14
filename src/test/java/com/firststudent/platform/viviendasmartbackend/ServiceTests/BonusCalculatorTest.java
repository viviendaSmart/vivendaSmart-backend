package com.firststudent.platform.viviendasmartbackend.ServiceTests;

import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.BonusCalculator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class BonusCalculatorTest {

    private final BonusCalculator bonusCalculator = new BonusCalculator();

    @Test
    void calculate_WhenBonusTypeIsNull_ShouldReturnZero() {
        BigDecimal result = bonusCalculator.calculate(
                new BigDecimal("2500"),
                new BigDecimal("60"),
                new BigDecimal("100000"),
                null
        );
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    void calculate_AVN_WhenSizeIsSmall_ShouldReturnFixedAmount() {
        // Arrange: Vivienda de 45 m2 (menor al límite de 50)
        BigDecimal propertySize = new BigDecimal("45");
        BigDecimal expected = new BigDecimal("46545");

        // Act
        BigDecimal actual = bonusCalculator.calculate(BigDecimal.ZERO, propertySize, BigDecimal.ZERO, "AVN");

        // Assert
        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    void calculate_AVN_TramosPorPrecio_ShouldReturnCorrectAmounts() {
        // Tramo 1: Precio <= 60000
        BigDecimal res1 = bonusCalculator.calculate(BigDecimal.ZERO, new BigDecimal("60"), new BigDecimal("55000"), "AVN");
        assertEquals(0, new BigDecimal("56710").compareTo(res1));

        // Tramo 2: 60000 < Precio <= 70000
        BigDecimal res2 = bonusCalculator.calculate(BigDecimal.ZERO, new BigDecimal("60"), new BigDecimal("65000"), "AVN");
        assertEquals(0, new BigDecimal("51895").compareTo(res2));

        // Tramo 3: 70000 < Precio <= 109000
        BigDecimal res3 = bonusCalculator.calculate(BigDecimal.ZERO, new BigDecimal("60"), new BigDecimal("100000"), "AVN");
        assertEquals(0, new BigDecimal("50825").compareTo(res3));

        // Tramo 4: 109000 < Precio <= 136000
        BigDecimal res4 = bonusCalculator.calculate(BigDecimal.ZERO, new BigDecimal("60"), new BigDecimal("130000"), "AVN");
        assertEquals(0, new BigDecimal("46545").compareTo(res4));
    }

    @Test
    void calculate_AVN_WhenPriceIsTooHigh_ShouldReturnZero() {
        // Arrange: Precio por encima de los límites de Techo Propio
        BigDecimal highPrice = new BigDecimal("200000");

        // Act
        BigDecimal result = bonusCalculator.calculate(BigDecimal.ZERO, new BigDecimal("60"), highPrice, "AVN");

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    void calculate_CSP_ShouldReturnFixedAmount() {
        // Act
        BigDecimal result = bonusCalculator.calculate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "CSP");

        // Assert
        assertEquals(0, new BigDecimal("32100").compareTo(result));
    }

    @Test
    void calculate_MV_ShouldReturnFixedAmount() {
        // Act
        BigDecimal result = bonusCalculator.calculate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "MV");

        // Assert
        assertEquals(0, new BigDecimal("12305").compareTo(result));
    }

    @Test
    void calculate_NONE_ShouldReturnZero() {
        // Act
        BigDecimal result = bonusCalculator.calculate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "NONE");

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }
}