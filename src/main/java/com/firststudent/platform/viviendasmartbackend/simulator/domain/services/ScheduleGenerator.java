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
            int n,
            String graceTypeRaw,   // <-- viene de Config
            int graceMonthsRaw     // <-- viene de Config
    ) {
        // Normalizamos tipo de gracia y meses de gracia
        String graceType = (graceTypeRaw != null)
                ? graceTypeRaw.trim().toUpperCase()
                : "NINGUNA";

        int graceMonths = graceMonthsRaw;
        if (graceMonths < 0) graceMonths = 0;
        if (graceMonths > n) graceMonths = n;

        // Tasas por período para seguros (ya calculadas en SimulationServiceImpl)
        BigDecimal lifeRate = nvl(result.getLifeInsuranceRatePeriod()); // TSD período (decimal)
        BigDecimal riskRate = nvl(result.getRiskInsuranceRatePeriod()); // TSR período (decimal)

        // Precio de la propiedad (debe venir en el SimulationResult)
        BigDecimal propertyPrice = nvl(result.getPropertyPrice());

        // Comisión periódica (% por período, en decimal)
        BigDecimal commissionRate = getPeriodicCommissionRate(request);

        List<SimulationResult.ScheduleItem> schedule = new ArrayList<>();
        BigDecimal balance = loanAmount;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPaidInstallments = BigDecimal.ZERO;
        BigDecimal totalPeriodicCostsReal = BigDecimal.ZERO;
        BigDecimal totalPrincipalAmortization = BigDecimal.ZERO;

        // Acumuladores desagregados
        BigDecimal totalLifeInsurance = BigDecimal.ZERO;
        BigDecimal totalRiskInsurance = BigDecimal.ZERO;
        BigDecimal totalPeriodicCommissions = BigDecimal.ZERO;
        BigDecimal totalPortes = BigDecimal.ZERO;

        // === FILA 0: desembolso ===
        SimulationResult.ScheduleItem row0 = new SimulationResult.ScheduleItem();
        row0.setPeriod(0);
        row0.setGraceFlag("");
        row0.setBeginningBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setInterest(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setPrincipal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setInstallment(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        row0.setLifeInsurance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setRiskInsurance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setPeriodicCommission(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        row0.setPeriodicCosts(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setTotalPeriodCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row0.setEndingBalance(loanAmount.setScale(2, RoundingMode.HALF_UP));
        row0.setCashFlow(loanAmount.setScale(2, RoundingMode.HALF_UP));

        schedule.add(row0);

        // ================== SIN GRACIA ==================
        if ("NINGUNA".equals(graceType) || graceMonths <= 0) {

            BigDecimal monthlyInstallment = computeInstallment(loanAmount, monthlyRate, n);
            result.setMonthlyInstallment(monthlyInstallment);

            for (int period = 1; period <= n; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setGraceFlag("");
                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal fixedPeriodicCosts = computePeriodicCostsForPeriod(request, period);

                BigDecimal lifeIns = balance.multiply(lifeRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal riskIns = propertyPrice.multiply(riskRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal comm = balance.multiply(commissionRate)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal totalPeriodCost = monthlyInstallment
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                BigDecimal endingBalance = balance.subtract(principal);

                if (period == n && endingBalance.compareTo(BigDecimal.ZERO) != 0) {
                    principal = balance;
                    interest = monthlyInstallment.subtract(principal)
                            .setScale(2, RoundingMode.HALF_UP);
                    endingBalance = BigDecimal.ZERO;
                }

                totalInterest = totalInterest.add(interest);
                totalPrincipalAmortization = totalPrincipalAmortization.add(principal);
                totalPaidInstallments = totalPaidInstallments.add(monthlyInstallment);

                totalPeriodicCostsReal = totalPeriodicCostsReal
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                // desagregados
                totalLifeInsurance = totalLifeInsurance.add(lifeIns);
                totalRiskInsurance = totalRiskInsurance.add(riskIns);
                totalPeriodicCommissions = totalPeriodicCommissions.add(comm);
                totalPortes = totalPortes.add(fixedPeriodicCosts);

                row.setInstallment(monthlyInstallment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));

                row.setLifeInsurance(lifeIns);
                row.setRiskInsurance(riskIns);
                row.setPeriodicCommission(comm);

                row.setPeriodicCosts(fixedPeriodicCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal cashFlow = totalPeriodCost.negate();
                row.setCashFlow(cashFlow.setScale(2, RoundingMode.HALF_UP));

                balance = endingBalance;
                schedule.add(row);
            }

            // ================== GRACIA PARCIAL ==================
        } else if ("PARCIAL".equals(graceType)) {

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
                row.setGraceFlag(period <= graceMonths ? "P" : "");
                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal;
                BigDecimal installment;

                if (period <= graceMonths) {
                    principal = BigDecimal.ZERO;
                    installment = interest;
                } else {
                    principal = monthlyInstallment.subtract(interest)
                            .setScale(2, RoundingMode.HALF_UP);
                    installment = monthlyInstallment;
                }

                BigDecimal fixedPeriodicCosts = computePeriodicCostsForPeriod(request, period);

                BigDecimal lifeIns = balance.multiply(lifeRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal riskIns = propertyPrice.multiply(riskRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal comm = balance.multiply(commissionRate)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal totalPeriodCost = installment
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                BigDecimal endingBalance = balance.subtract(principal);

                if (period == n && endingBalance.compareTo(BigDecimal.ZERO) != 0) {
                    principal = balance;
                    installment = principal.add(interest);
                    endingBalance = BigDecimal.ZERO;
                }

                totalInterest = totalInterest.add(interest);
                totalPrincipalAmortization = totalPrincipalAmortization.add(principal);
                totalPaidInstallments = totalPaidInstallments.add(installment);

                totalPeriodicCostsReal = totalPeriodicCostsReal
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                totalLifeInsurance = totalLifeInsurance.add(lifeIns);
                totalRiskInsurance = totalRiskInsurance.add(riskIns);
                totalPeriodicCommissions = totalPeriodicCommissions.add(comm);
                totalPortes = totalPortes.add(fixedPeriodicCosts);

                row.setInstallment(installment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));

                row.setLifeInsurance(lifeIns);
                row.setRiskInsurance(riskIns);
                row.setPeriodicCommission(comm);

                row.setPeriodicCosts(fixedPeriodicCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal cashFlow = totalPeriodCost.negate();
                row.setCashFlow(cashFlow.setScale(2, RoundingMode.HALF_UP));

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

            // Fase 1: meses de gracia
            for (int period = 1; period <= graceMonths; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setGraceFlag("T");

                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = BigDecimal.ZERO;
                BigDecimal installment = BigDecimal.ZERO;

                BigDecimal fixedPeriodicCosts = computePeriodicCostsForPeriod(request, period);

                BigDecimal lifeIns = balance.multiply(lifeRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal riskIns = propertyPrice.multiply(riskRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal comm = balance.multiply(commissionRate)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal totalPeriodCost = installment
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                BigDecimal endingBalance = balance.add(interest);

                totalInterest = totalInterest.add(interest);

                totalPeriodicCostsReal = totalPeriodicCostsReal
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                totalLifeInsurance = totalLifeInsurance.add(lifeIns);
                totalRiskInsurance = totalRiskInsurance.add(riskIns);
                totalPeriodicCommissions = totalPeriodicCommissions.add(comm);
                totalPortes = totalPortes.add(fixedPeriodicCosts);

                row.setInstallment(installment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));

                row.setLifeInsurance(lifeIns);
                row.setRiskInsurance(riskIns);
                row.setPeriodicCommission(comm);

                row.setPeriodicCosts(fixedPeriodicCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal cashFlow = totalPeriodCost.negate();
                row.setCashFlow(cashFlow.setScale(2, RoundingMode.HALF_UP));

                balance = endingBalance;
                schedule.add(row);
            }

            // Fase 2: francés sin gracia sobre el saldo capitalizado
            BigDecimal monthlyInstallment = computeInstallment(balance, monthlyRate, nEffective);
            result.setMonthlyInstallment(monthlyInstallment);

            for (int p = graceMonths + 1; p <= n; p++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(p);
                row.setGraceFlag("");

                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal fixedPeriodicCosts = computePeriodicCostsForPeriod(request, p);

                BigDecimal lifeIns = balance.multiply(lifeRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal riskIns = propertyPrice.multiply(riskRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal comm = balance.multiply(commissionRate)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal totalPeriodCost = monthlyInstallment
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                BigDecimal endingBalance = balance.subtract(principal);

                if (p == n && endingBalance.compareTo(BigDecimal.ZERO) != 0) {
                    principal = balance;
                    interest = monthlyInstallment.subtract(principal)
                            .setScale(2, RoundingMode.HALF_UP);
                    endingBalance = BigDecimal.ZERO;
                }

                totalInterest = totalInterest.add(interest);
                totalPrincipalAmortization = totalPrincipalAmortization.add(principal);
                totalPaidInstallments = totalPaidInstallments.add(monthlyInstallment);

                totalPeriodicCostsReal = totalPeriodicCostsReal
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                totalLifeInsurance = totalLifeInsurance.add(lifeIns);
                totalRiskInsurance = totalRiskInsurance.add(riskIns);
                totalPeriodicCommissions = totalPeriodicCommissions.add(comm);
                totalPortes = totalPortes.add(fixedPeriodicCosts);

                row.setInstallment(monthlyInstallment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));

                row.setLifeInsurance(lifeIns);
                row.setRiskInsurance(riskIns);
                row.setPeriodicCommission(comm);

                row.setPeriodicCosts(fixedPeriodicCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal cashFlow = totalPeriodCost.negate();
                row.setCashFlow(cashFlow.setScale(2, RoundingMode.HALF_UP));

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
                row.setGraceFlag("");

                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal fixedPeriodicCosts = computePeriodicCostsForPeriod(request, period);

                BigDecimal lifeIns = balance.multiply(lifeRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal riskIns = propertyPrice.multiply(riskRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal comm = balance.multiply(commissionRate)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal totalPeriodCost = monthlyInstallment
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                BigDecimal endingBalance = balance.subtract(principal);

                if (period == n && endingBalance.compareTo(BigDecimal.ZERO) != 0) {
                    principal = balance;
                    interest = monthlyInstallment.subtract(principal)
                            .setScale(2, RoundingMode.HALF_UP);
                    endingBalance = BigDecimal.ZERO;
                }

                totalInterest = totalInterest.add(interest);
                totalPrincipalAmortization = totalPrincipalAmortization.add(principal);
                totalPaidInstallments = totalPaidInstallments.add(monthlyInstallment);

                totalPeriodicCostsReal = totalPeriodicCostsReal
                        .add(fixedPeriodicCosts)
                        .add(lifeIns)
                        .add(riskIns)
                        .add(comm);

                totalLifeInsurance = totalLifeInsurance.add(lifeIns);
                totalRiskInsurance = totalRiskInsurance.add(riskIns);
                totalPeriodicCommissions = totalPeriodicCommissions.add(comm);
                totalPortes = totalPortes.add(fixedPeriodicCosts);

                row.setInstallment(monthlyInstallment.setScale(2, RoundingMode.HALF_UP));
                row.setInterest(interest);
                row.setPrincipal(principal.setScale(2, RoundingMode.HALF_UP));

                row.setLifeInsurance(lifeIns);
                row.setRiskInsurance(riskIns);
                row.setPeriodicCommission(comm);

                row.setPeriodicCosts(fixedPeriodicCosts.setScale(2, RoundingMode.HALF_UP));
                row.setTotalPeriodCost(totalPeriodCost.setScale(2, RoundingMode.HALF_UP));
                row.setEndingBalance(endingBalance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal cashFlow = totalPeriodCost.negate();
                row.setCashFlow(cashFlow.setScale(2, RoundingMode.HALF_UP));

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

        result.setTotalLifeInsurance(totalLifeInsurance.setScale(2, RoundingMode.HALF_UP));
        result.setTotalRiskInsurance(totalRiskInsurance.setScale(2, RoundingMode.HALF_UP));
        result.setTotalPeriodicCommissions(totalPeriodicCommissions.setScale(2, RoundingMode.HALF_UP));
        result.setTotalPortes(totalPortes.setScale(2, RoundingMode.HALF_UP));
    }

    // OJO: ya no existe computeGraceMonths, lo eliminamos

    private BigDecimal computePeriodicCostsForPeriod(SimulationRequest request, int period) {
        BigDecimal periodCosts = BigDecimal.ZERO;

        if (request.getCosts() != null) {
            for (SimulationRequest.CostItem item : request.getCosts()) {
                if (item.getType() == CostType.PERIODIC
                        && item.getCalcMode() == SimulationRequest.CostCalcMode.FIXED_AMOUNT
                        && item.getAmount() != null) {
                    Integer pn = item.getPeriodNumber();
                    if (pn == null || pn.equals(period)) {
                        periodCosts = periodCosts.add(item.getAmount());
                    }
                }
            }
        }
        return periodCosts;
    }

    private BigDecimal getPeriodicCommissionRate(SimulationRequest request) {
        if (request.getCosts() == null) return BigDecimal.ZERO;

        return request.getCosts().stream()
                .filter(c -> c.getCode() != null
                        && c.getCode().equalsIgnoreCase("COMISION_PERIODICA"))
                .filter(c -> c.getCalcMode() == SimulationRequest.CostCalcMode.PERCENTAGE)
                .map(c -> nvl(c.getAmount())
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal computeInstallment(BigDecimal loanAmount, BigDecimal monthlyRate, int n) {
        if (monthlyRate == null || monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
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
