package com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SimulationResult {

    // ====== Bloque: del financiamiento  ======

    // Saldo a financiar del activo = Precio - Precio*%Inicial - Valor BBP
    private BigDecimal financedBalance;

    // Valor BBP (bono)
    private BigDecimal bonusAmount;

    // Monto del préstamo = financedBalance + costos iniciales financiados
    private BigDecimal loanAmount;

    // Tasa efectiva por periodo del préstamo (en tu caso, mensual)
    private BigDecimal monthlyRate;

    // Cuota fija del método francés (cuando aplica)
    private BigDecimal monthlyInstallment;

    // Nº total de cuotas
    private Integer totalTerm;

    // Nº de cuotas por año
    private Integer installmentsPerYear;


    // ====== Bloque: costes/gastos periódicos (tasas por periodo) ======

    // % de Seguro desgravamen período (TSD)
    private BigDecimal lifeInsuranceRatePeriod;

    // % de Seguro de riesgo período (TSR)
    private BigDecimal riskInsuranceRatePeriod;


    // ====== Bloque: totales por concepto ======

    // Intereses totales del préstamo
    private BigDecimal totalInterest;

    // Suma de cuotas del préstamo (sin costos)
    private BigDecimal totalAmountPaid;

    // Amortización total del capital (en la hoja del profe)
    private BigDecimal totalPrincipalAmortization;

    // Totales de costos periódicos desagregados
    private BigDecimal totalLifeInsurance;        // Seguro de desgravamen
    private BigDecimal totalRiskInsurance;        // Seguro contra todo riesgo
    private BigDecimal totalPeriodicCommissions;  // Comisiones periódicas
    private BigDecimal totalPortes;               // Portes / gastos de administración

    // Totales agregados de costos
    private BigDecimal totalInitialCosts;         // suma de costos tipo INITIAL
    private BigDecimal totalPeriodicCosts;        // suma real de TODOS los PERIODIC
    // Costo total de la operación (lo que “sale del bolsillo” del cliente)
    // = totalAmountPaid + totalPeriodicCosts + totalInitialCosts
    private BigDecimal totalCost;


    // ====== Bloque: indicadores de rentabilidad ======

    // Tasa de descuento (Cok) por periodo
    private BigDecimal discountRatePeriod;

    // Valor Actual Neto de la operación
    private BigDecimal van;

    // TIR del periodo de la operación (en tu caso, mensual)
    private BigDecimal tir;

    // Tasa de Costo Efectiva Anual de la operación
    private BigDecimal tcea;


    // ====== Detalle de la tabla de amortización ======

    private List<ScheduleItem> schedule;

    @Getter
    @Setter
    public static class ScheduleItem {
        private Integer period;            // Nº
        private String graceFlag;          // P.G. ("T", "P" o "")

        private BigDecimal beginningBalance; // Saldo Inicial
        private BigDecimal installment;      // Cuota
        private BigDecimal interest;         // Interés
        private BigDecimal principal;        // Amort.
        private BigDecimal periodicCosts;    // (por ahora la suma de todos los costos)
        private BigDecimal totalPeriodCost;  // Cuota + costos
        private BigDecimal endingBalance;    // Saldo Final

    }
}
