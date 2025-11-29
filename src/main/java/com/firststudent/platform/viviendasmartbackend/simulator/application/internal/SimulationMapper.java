package com.firststudent.platform.viviendasmartbackend.simulator.application.internal;

import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates.*;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.valueobjects.CostCalcMode;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class SimulationMapper {

    public static Simulation toAggregate(  SimulationRequest request,
                                           SimulationResult result,
                                           BigDecimal configRate,
                                           String configRateType,
                                           String graceTypeCode,
                                           Integer graceMonths) {
        Simulation sim = new Simulation();

        // IDs básicos
        sim.setUserId(request.getUserId());
        sim.setClientId(request.getClientId());
        sim.setPropertyId(request.getPropertyId());

        // Inputs
        sim.setCurrency(result.getCurrency());
        sim.setPropertyPrice(result.getPropertyPrice());
        sim.setInitialPaymentPercent(request.getInitialPayment());
        sim.setTermYears(request.getTermYears());
        sim.setFrequencyDays(request.getFrequency());
        sim.setCokRate(request.getCokRate());
        sim.setCokRateType(request.getCokRateType());
        sim.setBonusType(request.getBonusType());

        sim.setRate(configRate);
        sim.setRateType(configRateType);
        sim.setGraceType(graceTypeCode);
        sim.setGraceMonths(graceMonths);

        // Resultados principales
        sim.setFinancedBalance(result.getFinancedBalance());
        sim.setBonusAmount(result.getBonusAmount());
        sim.setLoanAmount(result.getLoanAmount());
        sim.setPeriodRate(result.getMonthlyRate());
        sim.setInstallment(result.getMonthlyInstallment());
        sim.setTotalTerm(result.getTotalTerm());
        sim.setInstallmentsPerYear(result.getInstallmentsPerYear());

        sim.setLifeInsuranceRatePeriod(result.getLifeInsuranceRatePeriod());
        sim.setRiskInsuranceRatePeriod(result.getRiskInsuranceRatePeriod());

        sim.setTotalInterest(result.getTotalInterest());
        sim.setTotalAmountPaid(result.getTotalAmountPaid());
        sim.setTotalPrincipalAmortization(result.getTotalPrincipalAmortization());
        sim.setTotalLifeInsurance(result.getTotalLifeInsurance());
        sim.setTotalRiskInsurance(result.getTotalRiskInsurance());
        sim.setTotalPeriodicCommissions(result.getTotalPeriodicCommissions());
        sim.setTotalPortes(result.getTotalPortes());
        sim.setTotalInitialCosts(result.getTotalInitialCosts());
        sim.setTotalPeriodicCosts(result.getTotalPeriodicCosts());
        sim.setTotalCost(result.getTotalCost());

        sim.setDiscountRatePeriod(result.getDiscountRatePeriod());
        sim.setVan(result.getVan());
        sim.setTir(result.getTir());
        sim.setTcea(result.getTcea());

        // Costos
        if (request.getCosts() != null) {
            sim.setCosts(
                    request.getCosts().stream().map(c -> {
                        SimulationCostItem item = new SimulationCostItem();
                        item.setType(c.getType());
                        item.setCode(c.getCode());
                        item.setCalcMode(
                                c.getCalcMode() != null
                                        ? CostCalcMode.valueOf(c.getCalcMode().name())
                                        : null
                        );
                        item.setAmount(c.getAmount());
                        item.setPeriodNumber(c.getPeriodNumber());
                        return item;
                    }).collect(Collectors.toList())
            );
        }

        // Tabla de amortización
        if (result.getSchedule() != null) {
            result.getSchedule().forEach(row -> {
                SimulationScheduleItem item = new SimulationScheduleItem();
                item.setPeriod(row.getPeriod());
                item.setGraceFlag(row.getGraceFlag());
                item.setBeginningBalance(row.getBeginningBalance());
                item.setInstallment(row.getInstallment());
                item.setInterest(row.getInterest());
                item.setPrincipal(row.getPrincipal());
                item.setCashFlow(row.getCashFlow());
                item.setLifeInsurance(row.getLifeInsurance());
                item.setRiskInsurance(row.getRiskInsurance());
                item.setPeriodicCommission(row.getPeriodicCommission());
                item.setPeriodicCosts(row.getPeriodicCosts());
                item.setTotalPeriodCost(row.getTotalPeriodCost());
                item.setEndingBalance(row.getEndingBalance());
                sim.addScheduleItem(item);
            });
        }

        return sim;
    }
}
