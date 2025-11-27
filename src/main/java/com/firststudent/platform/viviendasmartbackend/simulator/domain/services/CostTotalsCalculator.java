package com.firststudent.platform.viviendasmartbackend.simulator.domain.services;

import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CostTotalsCalculator {

    public void computeCostTotals(SimulationRequest request,
                                  SimulationResult result,
                                  BigDecimal financedAssetBalance, // saldo a financiar del activo (S/)
                                  int totalPeriods) {

        BigDecimal totalInitialCosts = BigDecimal.ZERO;
        BigDecimal totalPeriodicConfig = BigDecimal.ZERO;

        BigDecimal totalLifeInsurance = BigDecimal.ZERO;
        BigDecimal totalRiskInsurance = BigDecimal.ZERO;
        BigDecimal totalPeriodicCommissions = BigDecimal.ZERO;
        BigDecimal totalPortes = BigDecimal.ZERO;

        if (request.getCosts() != null) {
            for (SimulationRequest.CostItem item : request.getCosts()) {

                BigDecimal effectiveAmount = resolveAmount(item, financedAssetBalance);
                if (effectiveAmount == null) continue;

                String code = item.getCode() != null
                        ? item.getCode().trim().toUpperCase()
                        : "";

                if (item.getType() == CostType.INITIAL) {

                    // 🔹 Todos los costos iniciales ya vienen convertidos a S/
                    totalInitialCosts = totalInitialCosts.add(effectiveAmount);

                } else if (item.getType() == CostType.PERIODIC) {

                    // 🔹 Monto configurado por periodo (mensual)
                    totalPeriodicConfig = totalPeriodicConfig.add(effectiveAmount);

                    int occurrences = (item.getPeriodNumber() == null) ? totalPeriods : 1;

                    BigDecimal totalForItem = effectiveAmount
                            .multiply(BigDecimal.valueOf(occurrences));

                    switch (code) {
                        case "DESGRAVAMEN":
                        case "SEGURO_DESGRAVAMEN":
                            totalLifeInsurance = totalLifeInsurance.add(totalForItem);
                            break;
                        case "RIESGO":
                        case "SEGURO_RIESGO":
                            totalRiskInsurance = totalRiskInsurance.add(totalForItem);
                            break;
                        case "COMISION":
                        case "COMISION_PERIODICA":
                            totalPeriodicCommissions = totalPeriodicCommissions.add(totalForItem);
                            break;
                        case "PORTES":
                        case "GASTOS_ADMIN":
                            totalPortes = totalPortes.add(totalForItem);
                            break;
                        default:
                            // otros costos periódicos genéricos
                            break;
                    }
                }
            }
        }

        result.setTotalInitialCosts(totalInitialCosts.setScale(2, RoundingMode.HALF_UP));
        // total periódico "configurado" (por periodo); luego se reemplaza por lo realmente pagado
        result.setTotalPeriodicCosts(totalPeriodicConfig.setScale(2, RoundingMode.HALF_UP));

        result.setTotalLifeInsurance(totalLifeInsurance.setScale(2, RoundingMode.HALF_UP));
        result.setTotalRiskInsurance(totalRiskInsurance.setScale(2, RoundingMode.HALF_UP));
        result.setTotalPeriodicCommissions(totalPeriodicCommissions.setScale(2, RoundingMode.HALF_UP));
        result.setTotalPortes(totalPortes.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Resuelve el monto efectivo en S/ según:
     * - type: INITIAL / PERIODIC
     * - calcMode: FIXED_AMOUNT / PERCENTAGE
     * - amount: valor enviado por el front (S/ o %)
     *
     * Por ahora:
     *  - FIXED_AMOUNT → se toma tal cual.
     *  - PERCENTAGE + INITIAL → % del saldo a financiar del activo.
     *  - PERCENTAGE + PERIODIC → se devuelve 0 (lo real se calculará en el ScheduleGenerator).
     */
    private BigDecimal resolveAmount(SimulationRequest.CostItem item,
                                     BigDecimal financedAssetBalance) {

        BigDecimal amount = item.getAmount();
        if (amount == null) return null;

        SimulationRequest.CostCalcMode calcMode = item.getCalcMode();
        CostType type = item.getType();

        if (calcMode == null) {
            // Default conservador: lo tratamos como monto fijo en S/
            return amount;
        }

        switch (calcMode) {
            case FIXED_AMOUNT:
                // Monto ya viene en S/
                return amount;

            case PERCENTAGE:
                // Hoy solo usamos % para costos INICIALES (Comisión Estudio / Activación, etc.)
                if (type == CostType.INITIAL) {
                    return computePercentOverFinancedAsset(amount, financedAssetBalance);
                }
                // Para PERIODIC + PERCENTAGE (ej. seguro desgravamen % sobre saldo),
                // el cálculo real se hará en el ScheduleGenerator, no aquí.
                return BigDecimal.ZERO;

            default:
                return amount;
        }
    }

    /**
     * Interpreta amount como porcentaje del saldo a financiar del activo.
     *
     * Si amount <= 1   → se asume fracción (0.015 = 1.5%)
     * Si amount > 1    → se asume porcentaje (1.5 = 1.5%, 2 = 2%) y se divide entre 100
     */
    private BigDecimal computePercentOverFinancedAsset(BigDecimal amount,
                                                       BigDecimal financedAssetBalance) {

        if (financedAssetBalance == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal fraction;
        if (amount.compareTo(BigDecimal.ONE) <= 0) {
            // Ej: 0.015 → 1.5%
            fraction = amount;
        } else {
            // Ej: 1.5 → 1.5% → 0.015
            fraction = amount
                    .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        }

        return financedAssetBalance
                .multiply(fraction)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
