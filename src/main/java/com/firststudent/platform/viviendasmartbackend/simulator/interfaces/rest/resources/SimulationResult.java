package com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SimulationResult {

    // Resumen del crédito
    private BigDecimal loanAmount;         // monto del préstamo (price - inicial - bono, etc.)
    private BigDecimal monthlyRate;        // tasa efectiva mensual usada en el cálculo
    private BigDecimal monthlyInstallment; // cuota fija del método francés (cuando aplica)
    private Integer termMonths;            // número de meses

    // Totales de la operación (solo préstamo)
    private BigDecimal totalInterest;      // suma de intereses de todo el período
    private BigDecimal totalAmountPaid;    // suma de cuotas del préstamo (sin costos)

    // Indicadores financieros
    private BigDecimal van;                // Valor Actual Neto
    private BigDecimal tir;                // Tasa Interna de Retorno (mensual)
    private BigDecimal tcea;              // Tasa de Costo Efectiva Anual

    // Costos asociados
    private BigDecimal totalInitialCosts;  // suma de costos tipo INITIAL configurados
    private BigDecimal totalPeriodicCosts; // suma real de costos PERIODIC pagados en todo el plazo

    // Costo total de la operación (lo que “sale del bolsillo” del cliente)
    // = totalAmountPaid + totalPeriodicCosts + totalInitialCosts
    private BigDecimal totalCost;

    // Detalle de la tabla de amortización
    private List<ScheduleItem> schedule;

    @Getter
    @Setter
    public static class ScheduleItem {
        private Integer period;            // 1, 2, 3, ...
        private BigDecimal beginningBalance;
        private BigDecimal installment;    // cuota del periodo (solo préstamo)
        private BigDecimal interest;       // parte de interés
        private BigDecimal principal;      // parte de amortización
        private BigDecimal periodicCosts;  // suma de costos periódicos en ese periodo

        // Costo total del periodo = cuota + costos periódicos
        private BigDecimal totalPeriodCost;

        private BigDecimal endingBalance;
    }
}
