package com.firststudent.platform.viviendasmartbackend.simulator.domain.services;

import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialIndicatorsCalculator {

    /**
     * Calcula VAN, TIR (mensual) y TCEA a partir del resultado de simulación,
     * el monto del préstamo y la tasa de descuento mensual (COK mensual).
     */
    public void computeVanTirTcea(SimulationResult result,
                                  BigDecimal loanAmount,
                                  BigDecimal discountRateMonthly) {

        List<SimulationResult.ScheduleItem> schedule = result.getSchedule();
        if (schedule == null || schedule.isEmpty()) {
            result.setVan(BigDecimal.ZERO);
            result.setTir(BigDecimal.ZERO);
            result.setTcea(BigDecimal.ZERO);
            return;
        }

        BigDecimal initialCosts = nvl(result.getTotalInitialCosts());

        // Flujo 0: inversión (préstamo + costos iniciales) con signo negativo
        BigDecimal inversion = loanAmount.add(initialCosts);

        List<BigDecimal> cashFlows = new ArrayList<>();
        cashFlows.add(inversion.negate()); // CF0

        // CFt = cuota del periodo + costos periódicos del periodo
        for (SimulationResult.ScheduleItem row : schedule) {
            BigDecimal cf = nvl(row.getInstallment()).add(nvl(row.getPeriodicCosts()));
            cashFlows.add(cf);
        }

        // VAN usando la tasa efectiva mensual de descuento (COK mensual)
        BigDecimal van = computeNPV(discountRateMonthly, cashFlows);
        result.setVan(van.setScale(2, RoundingMode.HALF_UP));

        // TIR (mensual) a partir de los mismos flujos
        BigDecimal tir = computeIRR(cashFlows);
        result.setTir(tir != null ? tir.setScale(10, RoundingMode.HALF_UP) : null);

        // TCEA = (1 + TIR_mensual)^12 - 1
        if (tir != null) {
            double irrMonthly = tir.doubleValue();
            double tceaDouble = Math.pow(1 + irrMonthly, 12.0) - 1.0;
            BigDecimal tcea = BigDecimal.valueOf(tceaDouble);
            result.setTcea(tcea.setScale(10, RoundingMode.HALF_UP));
        } else {
            result.setTcea(null);
        }
    }

    // =================== Helpers internos ===================

    private BigDecimal computeNPV(BigDecimal discountRate, List<BigDecimal> cashFlows) {
        if (discountRate == null) discountRate = BigDecimal.ZERO;
        double r = discountRate.doubleValue();

        double npv = 0.0;
        for (int t = 0; t < cashFlows.size(); t++) {
            double cf = cashFlows.get(t).doubleValue();
            npv += cf / Math.pow(1 + r, t);
        }
        return BigDecimal.valueOf(npv);
    }

    private BigDecimal computeIRR(List<BigDecimal> cashFlows) {
        double[] flows = new double[cashFlows.size()];
        for (int i = 0; i < cashFlows.size(); i++) {
            flows[i] = cashFlows.get(i).doubleValue();
        }

        // Rangos de búsqueda: de -0.9 a 10
        double low = -0.9;
        double high = 10.0;
        double fLow = npv(low, flows);
        double fHigh = npv(high, flows);

        // Si no hay cambio de signo, no garantizamos raíz
        if (fLow * fHigh > 0) {
            return null;
        }

        double mid = 0;
        for (int i = 0; i < 100; i++) {
            mid = (low + high) / 2.0;
            double fMid = npv(mid, flows);

            if (Math.abs(fMid) < 1e-8) {
                break;
            }

            if (fLow * fMid < 0) {
                high = mid;
                fHigh = fMid;
            } else {
                low = mid;
                fLow = fMid;
            }
        }

        return BigDecimal.valueOf(mid);
    }

    private double npv(double rate, double[] cashFlows) {
        double npv = 0.0;
        for (int t = 0; t < cashFlows.length; t++) {
            npv += cashFlows[t] / Math.pow(1 + rate, t);
        }
        return npv;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
