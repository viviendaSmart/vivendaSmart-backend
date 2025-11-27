package com.firststudent.platform.viviendasmartbackend.simulator.domain.services;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RateConverter {

    /**
     * Convierte cualquier tipo de tasa a TEM (tasa efectiva mensual), en DECIMAL.
     */
    public BigDecimal toMonthly(BigDecimal ratePercent, String rateType) {
        if (ratePercent == null || rateType == null) return BigDecimal.ZERO;

        // Convertir de % a decimal: 9.5  ->  0.095
        BigDecimal rate = ratePercent
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        String t = rateType.trim().toUpperCase();

        switch (t) {
            case "TEM": return rate; // ya es mensual y en decimal
            case "TEA": return effectiveToMonthly(rate, 12);
            case "TET": return effectiveToMonthly(rate, 3);
            case "TES": return effectiveToMonthly(rate, 6);
            case "TEB": return effectiveToMonthly(rate, 2);
            case "TNA": return nominalToMonthly(rate, 12, 1);
            case "TNT": return nominalToMonthly(rate, 4, 3);
            case "TNS": return nominalToMonthly(rate, 2, 6);
            case "TNB": return nominalToMonthly(rate, 6, 2);
            default:
                // fallback: dividir entre 12 una tasa nominal anual ya en decimal
                return rate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        }
    }

    /**
     * Convierte cualquier tipo de tasa a TEA (tasa efectiva anual), en DECIMAL.
     */
    public BigDecimal toYearly(BigDecimal ratePercent, String rateType) {
        if (ratePercent == null || rateType == null) return BigDecimal.ZERO;

        // De % a decimal
        BigDecimal rate = ratePercent
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        String t = rateType.trim().toUpperCase();

        switch (t) {
            case "TEA":
                // Ya es efectiva anual
                return rate;

            case "TEM":
                // Efectiva mensual -> anual: (1 + i_m)^12 - 1
                return effectiveToAnnual(rate, 12);

            case "TET":
                // Efectiva trimestral (3 meses) -> anual: 4 periodos por año
                return effectiveToAnnual(rate, 4);

            case "TES":
                // Efectiva semestral (6 meses) -> anual: 2 periodos por año
                return effectiveToAnnual(rate, 2);

            case "TEB":
                // Efectiva bimestral (2 meses) -> anual: 6 periodos por año
                return effectiveToAnnual(rate, 6);

            case "TNA":
                // Nominal anual capitalizable mensualmente: 12 periodos/año
                return nominalToAnnual(rate, 12);

            case "TNT":
                // Nominal trimestral (4 periodos/año)
                return nominalToAnnual(rate, 4);

            case "TNS":
                // Nominal semestral (2 periodos/año)
                return nominalToAnnual(rate, 2);

            case "TNB":
                // Nominal bimestral (6 periodos/año)
                return nominalToAnnual(rate, 6);

            default:
                // Si no reconocemos el tipo, asumimos que ya es TEA
                return rate;
        }
    }

    // ===== Helpers internos =====

    private BigDecimal effectiveToMonthly(BigDecimal rate, int monthsPerPeriod) {
        double r = rate.doubleValue(); // rate ya es decimal (0.095, no 9.5)
        double tem = Math.pow(1 + r, 1.0 / monthsPerPeriod) - 1.0;
        return BigDecimal.valueOf(tem).setScale(10, RoundingMode.HALF_UP);
    }

    private BigDecimal nominalToMonthly(BigDecimal rate, int periodsPerYear, int monthsPerPeriod) {
        double iNom = rate.doubleValue(); // también decimal (0.095)
        double iPer = iNom / periodsPerYear;
        double tem = Math.pow(1 + iPer, 1.0 / monthsPerPeriod) - 1.0;
        return BigDecimal.valueOf(tem).setScale(10, RoundingMode.HALF_UP);
    }

    private BigDecimal effectiveToAnnual(BigDecimal rate, int periodsPerYear) {
        double r = rate.doubleValue();
        double tea = Math.pow(1 + r, periodsPerYear) - 1.0;
        return BigDecimal.valueOf(tea).setScale(10, RoundingMode.HALF_UP);
    }

    private BigDecimal nominalToAnnual(BigDecimal rate, int periodsPerYear) {
        double iNom = rate.doubleValue();
        double iPer = iNom / periodsPerYear;
        double tea = Math.pow(1 + iPer, periodsPerYear) - 1.0;
        return BigDecimal.valueOf(tea).setScale(10, RoundingMode.HALF_UP);
    }

    public BigDecimal yearlyToPeriod(BigDecimal yearlyRate, int daysPerPeriod) {
        if (yearlyRate == null) return BigDecimal.ZERO;
        if (daysPerPeriod <= 0) {
            throw new IllegalArgumentException("daysPerPeriod must be > 0");
        }

        double r = yearlyRate.doubleValue(); // ej. 0.05
        double exponent = (double) daysPerPeriod / 360.0; // ej. 30/360 = 1/12
        double iPer = Math.pow(1 + r, exponent) - 1.0;

        return BigDecimal
                .valueOf(iPer)
                .setScale(10, RoundingMode.HALF_UP);
    }

}

