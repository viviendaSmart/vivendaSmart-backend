package com.firststudent.platform.viviendasmartbackend.simulator.domain.services;

import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleGenerator {

    public void buildScheduleAndTotals(
            SimulationRequest request,
            SimulationResult result,
            BigDecimal loanAmount,
            BigDecimal monthlyRate,
            int n
    ) {
        String graceType = request.getGraceType() != null
                ? request.getGraceType().trim().toUpperCase()
                : "NINGUNA";

        int graceMonths = computeGraceMonths(request, n);

        List<SimulationResult.ScheduleItem> schedule = new ArrayList<>();
        BigDecimal balance = loanAmount;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPaidInstallments = BigDecimal.ZERO;
        BigDecimal totalPeriodicCostsReal = BigDecimal.ZERO;
        BigDecimal totalPrincipalAmortization = BigDecimal.ZERO;

        // === FILA 0: desembolso ===
        SimulationResult.ScheduleItem row0 = new SimulationResult.ScheduleItem();
        row0.setPeriod(0);
        row0.setGraceFlag(""); // en la fila 0 no hay P.G.
        row0.setBeginningBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setInterest(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setPrincipal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setInstallment(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setPeriodicCosts(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setTotalPeriodCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        // Saldo final después del desembolso = monto del préstamo
        row0.setEndingBalance(loanAmount.setScale(2, RoundingMode.HALF_UP));
        schedule.add(row0);

        // ================== SIN GRACIA ==================
        if ("NINGUNA".equals(graceType) || graceMonths <= 0) {

            BigDecimal monthlyInstallment = computeInstallment(loanAmount, monthlyRate, n);
            result.setMonthlyInstallment(monthlyInstallment);

            for (int period = 1; period <= n; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setGraceFlag(""); // sin periodo de gracia
                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, period);
                BigDecimal totalPeriodCost = monthlyInstallment.add(periodCosts);

                BigDecimal endingBalance = balance.subtract(principal);

                // Ajuste final por redondeo
                if (period == n && endingBalance.compareTo(BigDecimal.ZERO) != 0) {
                    principal = balance;
                    interest = monthlyInstallment.subtract(principal)
                            .setScale(2, RoundingMode.HALF_UP);
                    endingBalance = BigDecimal.ZERO;
                }

                totalInterest = totalInterest.add(interest);
                totalPrincipalAmortization = totalPrincipalAmortization.add(principal);
                totalPaidInstallments = totalPaidInstallments.add(monthlyInstallment);
                totalPeriodicCostsReal = totalPeriodicCostsReal.add(periodCosts);

                row.setInstallment(monthlyInstallment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));
                row.setPeriodicCosts(periodCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                balance = endingBalance;
                schedule.add(row);
            }

            // ================== GRACIA PARCIAL ==================
        } else if ("PARCIAL".equals(graceType)) {

            // primeros meses solo interés, luego francés normal
            int nEffective = n - graceMonths;
            if (nEffective <= 0) {
                nEffective = n;
                graceMonths = 0;
            }

            BigDecimal monthlyInstallment = computeInstallment(loanAmount, monthlyRate, nEffective);
            result.setMonthlyInstallment(monthlyInstallment);

            for (int period = 1; period <= n; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setGraceFlag(period <= graceMonths ? "P" : ""); // P.G.

                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal;
                BigDecimal installment;

                if (period <= graceMonths) {
                    // SOLO INTERESES
                    principal = BigDecimal.ZERO;
                    installment = interest;
                } else {
                    // Método francés normal
                    principal = monthlyInstallment.subtract(interest)
                            .setScale(2, RoundingMode.HALF_UP);
                    installment = monthlyInstallment;
                }

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, period);
                BigDecimal totalPeriodCost = installment.add(periodCosts);

                BigDecimal endingBalance = balance.subtract(principal);

                // Ajuste final por redondeo
                if (period == n && endingBalance.compareTo(BigDecimal.ZERO) != 0) {
                    principal = balance;
                    installment = principal.add(interest);
                    endingBalance = BigDecimal.ZERO;
                }

                totalInterest = totalInterest.add(interest);
                totalPrincipalAmortization = totalPrincipalAmortization.add(principal);
                totalPaidInstallments = totalPaidInstallments.add(installment);
                totalPeriodicCostsReal = totalPeriodicCostsReal.add(periodCosts);

                row.setInstallment(installment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));
                row.setPeriodicCosts(periodCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                balance = endingBalance;
                schedule.add(row);
            }

            // ================== GRACIA TOTAL ==================
        } else if ("TOTAL".equals(graceType)) {

            int nEffective = n - graceMonths;
            if (nEffective <= 0) {
                nEffective = n;
                graceMonths = 0;
            }

            // Fase 1: meses de gracia (capitalización de intereses)
            for (int period = 1; period <= graceMonths; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setGraceFlag("T"); // P.G. total

                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = BigDecimal.ZERO;
                BigDecimal installment = BigDecimal.ZERO; // no se paga cuota

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, period);
                BigDecimal totalPeriodCost = installment.add(periodCosts);

                // Intereses se capitalizan
                BigDecimal endingBalance = balance.add(interest);

                totalInterest = totalInterest.add(interest);
                totalPeriodicCostsReal = totalPeriodicCostsReal.add(periodCosts);

                row.setInstallment(installment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));
                row.setPeriodicCosts(periodCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                balance = endingBalance;
                schedule.add(row);
            }

            // Fase 2: método francés sobre el saldo capitalizado, en nEffective meses
            BigDecimal monthlyInstallment = computeInstallment(balance, monthlyRate, nEffective);
            result.setMonthlyInstallment(monthlyInstallment);

            for (int p = graceMonths + 1; p <= n; p++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(p);
                row.setGraceFlag(""); // ya no está en gracia

                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, p);
                BigDecimal totalPeriodCost = monthlyInstallment.add(periodCosts);

                BigDecimal endingBalance = balance.subtract(principal);

                // Ajuste final por redondeo
                if (p == n && endingBalance.compareTo(BigDecimal.ZERO) != 0) {
                    principal = balance;
                    interest = monthlyInstallment.subtract(principal)
                            .setScale(2, RoundingMode.HALF_UP);
                    endingBalance = BigDecimal.ZERO;
                }

                totalInterest = totalInterest.add(interest);
                totalPrincipalAmortization = totalPrincipalAmortization.add(principal);
                totalPaidInstallments = totalPaidInstallments.add(monthlyInstallment);
                totalPeriodicCostsReal = totalPeriodicCostsReal.add(periodCosts);

                row.setInstallment(monthlyInstallment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));
                row.setPeriodicCosts(periodCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                balance = endingBalance;
                schedule.add(row);
            }

            // ================== CUALQUIER OTRO TIPO ==================
        } else {

            BigDecimal monthlyInstallment = computeInstallment(loanAmount, monthlyRate, n);
            result.setMonthlyInstallment(monthlyInstallment);

            for (int period = 1; period <= n; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setGraceFlag(""); // lo tratamos como sin gracia

                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, period);
                BigDecimal totalPeriodCost = monthlyInstallment.add(periodCosts);

                BigDecimal endingBalance = balance.subtract(principal);

                // Ajuste final por redondeo
                if (period == n && endingBalance.compareTo(BigDecimal.ZERO) != 0) {
                    principal = balance;
                    interest = monthlyInstallment.subtract(principal)
                            .setScale(2, RoundingMode.HALF_UP);
                    endingBalance = BigDecimal.ZERO;
                }

                totalInterest = totalInterest.add(interest);
                totalPrincipalAmortization = totalPrincipalAmortization.add(principal);
                totalPaidInstallments = totalPaidInstallments.add(monthlyInstallment);
                totalPeriodicCostsReal = totalPeriodicCostsReal.add(periodCosts);

                row.setInstallment(monthlyInstallment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));
                row.setPeriodicCosts(periodCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                balance = endingBalance;
                schedule.add(row);
            }
        }

        // ===== Totales en el resultado =====
        result.setSchedule(schedule);
        result.setTotalInterest(totalInterest.setScale(2, RoundingMode.HALF_UP));
        result.setTotalAmountPaid(totalPaidInstallments.setScale(2, RoundingMode.HALF_UP));
        result.setTotalPeriodicCosts(totalPeriodicCostsReal.setScale(2, RoundingMode.HALF_UP));
        result.setTotalPrincipalAmortization(totalPrincipalAmortization.setScale(2, RoundingMode.HALF_UP));

        BigDecimal totalInitialCosts = nvl(result.getTotalInitialCosts());
        BigDecimal totalCost = totalPaidInstallments
                .add(totalPeriodicCostsReal)
                .add(totalInitialCosts);

        result.setTotalCost(totalCost.setScale(2, RoundingMode.HALF_UP));
    }

    private int computeGraceMonths(SimulationRequest request, int termMonths) {
        String graceType = request.getGraceType();
        if (graceType == null || graceType.equalsIgnoreCase("NINGUNA")) return 0;

        String termDaysStr = request.getTerm(); // días de gracia
        if (termDaysStr == null) return 0;

        try {
            int days = Integer.parseInt(termDaysStr.trim());
            if (days <= 0) return 0;

            // Aproximamos meses de gracia como ceil(días / 30)
            int months = (int) Math.ceil(days / 30.0);
            if (months > termMonths) months = termMonths;
            return months;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal computePeriodicCostsForPeriod(SimulationRequest request, int period) {
        BigDecimal periodCosts = BigDecimal.ZERO;

        if (request.getCosts() != null) {
            for (SimulationRequest.CostItem item : request.getCosts()) {
                if (item.getType() == CostType.PERIODIC && item.getAmount() != null) {
                    Integer pn = item.getPeriodNumber();
                    if (pn == null || pn.equals(period)) {
                        periodCosts = periodCosts.add(item.getAmount());
                    }
                }
            }
        }
        return periodCosts;
    }

    private BigDecimal computeInstallment(BigDecimal loanAmount, BigDecimal monthlyRate, int n) {
        if (monthlyRate == null || monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // Si la tasa es 0, cuota = capital / n
            return loanAmount
                    .divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        }

        double P = loanAmount.doubleValue();
        double i = monthlyRate.doubleValue();
        double cuota = P * (i / (1 - Math.pow(1 + i, -n)));

        return BigDecimal.valueOf(cuota).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
