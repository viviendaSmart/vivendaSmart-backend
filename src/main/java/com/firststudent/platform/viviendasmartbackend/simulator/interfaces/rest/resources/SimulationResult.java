package com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SimulationResult {

    // ====== Bloque: del financiamiento (profe) ======

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

    // Nº total de cuotas (en la hoja del profe: N° Total de Cuotas)
    private Integer termMonths;

    // Nº de cuotas por año (en la hoja del profe: N° Cuotas por Año)
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
        private Integer period;            // 1, 2, 3, ...
        private BigDecimal beginningBalance;
        private BigDecimal installment;    // cuota del periodo (solo préstamo)
        private BigDecimal interest;       // parte de interés
        private BigDecimal principal;      // parte de amortización
        private BigDecimal periodicCosts;  // suma de costos periódicos del periodo
        private BigDecimal totalPeriodCost;// cuota + costos periódicos
        private BigDecimal endingBalance;
    }
}
