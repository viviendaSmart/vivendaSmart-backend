package com.firststudent.platform.viviendasmartbackend.ServiceTests;

import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.RateConverter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RateConverterTest {

    private final RateConverter rateConverter = new RateConverter();

    @Test
    void toMonthly_WhenNullInputs_ShouldReturnZero() {
        // Act
        BigDecimal result1 = rateConverter.toMonthly(null, "TEA");
        BigDecimal result2 = rateConverter.toMonthly(new BigDecimal("10"), null);

        // Assert
        assertEquals(BigDecimal.ZERO, result1);
        assertEquals(BigDecimal.ZERO, result2);
    }

    @Test
    void toMonthly_FromTEA_ShouldCalculateTEM() {
        // Arrange: 10% TEA
        BigDecimal ratePercent = new BigDecimal("10");

        // El cálculo matemático es: (1 + 0.10)^(1/12) - 1 = 0.0079741404...
        BigDecimal expectedTem = new BigDecimal("0.0079741404");

        // Act
        BigDecimal actualTem = rateConverter.toMonthly(ratePercent, "TEA");

        // Assert
        // Usamos compareTo() == 0 porque BigDecimal.equals() es muy estricto con la escala interna
        assertEquals(0, expectedTem.compareTo(actualTem),
                "La conversión de 10% TEA a TEM debería ser 0.0079741404");
    }

    @Test
    void toMonthly_FromTNA_ShouldCalculateTEM() {
        // Arrange: 12% TNA (Capitalización mensual por defecto en tu código)
        BigDecimal ratePercent = new BigDecimal("12");

        // TNA a TEM directo (12% / 12 meses) = 1% = 0.01
        BigDecimal expectedTem = new BigDecimal("0.0100000000");

        // Act
        BigDecimal actualTem = rateConverter.toMonthly(ratePercent, "TNA");

        // Assert
        assertEquals(0, expectedTem.compareTo(actualTem),
                "La conversión de 12% TNA a TEM debería ser exactamente 0.01");
    }

    @Test
    void toYearly_FromTEM_ShouldCalculateTEA() {
        // Arrange: 1% TEM
        BigDecimal ratePercent = new BigDecimal("1");

        // El cálculo matemático es: (1 + 0.01)^12 - 1 = 0.1268250301...
        BigDecimal expectedTea = new BigDecimal("0.1268250301");

        // Act
        BigDecimal actualTea = rateConverter.toYearly(ratePercent, "TEM");

        // Assert
        assertEquals(0, expectedTea.compareTo(actualTea),
                "La conversión de 1% TEM a TEA debería ser 0.1268250301");
    }

    @Test
    void yearlyToPeriod_WithValidDays_ShouldCalculateCorrectPeriodRate() {
        // Arrange: 10% TEA (Ya en decimal según tu método: 0.10)
        BigDecimal yearlyRate = new BigDecimal("0.10");
        int daysPerPeriod = 30; // 1 mes (30/360)

        // (1 + 0.10)^(30/360) - 1 = 0.0079741404
        BigDecimal expectedPeriodRate = new BigDecimal("0.0079741404");

        // Act
        BigDecimal actualPeriodRate = rateConverter.yearlyToPeriod(yearlyRate, daysPerPeriod);

        // Assert
        assertEquals(0, expectedPeriodRate.compareTo(actualPeriodRate));
    }

    @Test
    void yearlyToPeriod_WithInvalidDays_ShouldThrowException() {
        // Arrange
        BigDecimal yearlyRate = new BigDecimal("0.10");
        int invalidDays = 0; // o negativo

        // Act & Assert
        // Verificamos que tu código lance la excepción correctamente para proteger el sistema
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rateConverter.yearlyToPeriod(yearlyRate, invalidDays);
        });

        assertEquals("daysPerPeriod must be > 0", exception.getMessage());
    }
}
