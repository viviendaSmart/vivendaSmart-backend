package com.firststudent.platform.viviendasmartbackend.simulator.application.internal.queryservices;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientQueryService;
import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;
import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.domain.services.PropertyQueryService;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.SimulationService;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationServiceImpl implements SimulationService {

    private final PropertyQueryService propertyQueryService;
    private final ClientQueryService clientQueryService;

    public SimulationServiceImpl(PropertyQueryService propertyQueryService,
                                 ClientQueryService clientQueryService) {
        this.propertyQueryService = propertyQueryService;
        this.clientQueryService = clientQueryService;
    }

    @Override
    public SimulationResult simulate(SimulationRequest request) {

        // PASO 1: Obtener propiedad y precio desde BD
        Property property = getProperty(request);
        BigDecimal price = nvl(property.getPrice());
        BigDecimal propertySize = nvl(property.getSize());

        // PASO 1.1: Obtener cliente e ingresos
        Client client = getClient(request);
        BigDecimal monthlyIncome = nvl(client.getMonthlyIncome());

        // PASO 2: Calcular monto del bono según reglas (AVN, CSP, MV)
        BigDecimal bonusAmount = computeBonusAmount(
                monthlyIncome,
                propertySize,
                price,
                request.getBonusType()
        );

        // PASO 3: Saldo a financiar del activo (Precio - Inicial - Bono)
        BigDecimal financedBalance = computeSaldoAFinanciar(
                price,
                request.getInitialPayment(),
                bonusAmount
        );

        // PASO 4: Validar y obtener plazo (n)
        int n = getValidatedTermMonths(request);

        SimulationResult result = new SimulationResult();
        result.setFinancedBalance(financedBalance);
        result.setBonusAmount(bonusAmount);
        result.setTermMonths(n);
        // Asumimos frecuencia mensual
        result.setInstallmentsPerYear(12);

        // PASO 5: Calcular totales de costos (iniciales + periódicos configurados)
        computeCostTotals(request, result, n);

        BigDecimal totalInitialCosts = nvl(result.getTotalInitialCosts());

        // PASO 6: Monto del préstamo = saldo a financiar + costos iniciales
        BigDecimal loanAmount = financedBalance.add(totalInitialCosts);
        if (loanAmount.compareTo(BigDecimal.ZERO) < 0) {
            loanAmount = BigDecimal.ZERO;
        }
        result.setLoanAmount(loanAmount);

        // PASO 7: Tasa efectiva mensual del préstamo
        BigDecimal rate = nvl(request.getRate());
        BigDecimal monthlyRate = computeMonthlyRate(rate, request.getRateType());
        result.setMonthlyRate(monthlyRate);

        // PASO 7.1: Tasa de descuento mensual (COK período)
        BigDecimal cokRate = nvl(request.getCokRate());
        BigDecimal discountRatePeriod = computeMonthlyRate(cokRate, request.getCokRateType());
        result.setDiscountRatePeriod(discountRatePeriod);

        // Si no hay préstamo, devolvemos resultado mínimo
        if (loanAmount.compareTo(BigDecimal.ZERO) == 0) {
            result.setMonthlyInstallment(BigDecimal.ZERO);
            result.setTotalInterest(BigDecimal.ZERO);
            result.setTotalAmountPaid(BigDecimal.ZERO);
            result.setTotalPrincipalAmortization(BigDecimal.ZERO);
            result.setTotalPeriodicCosts(BigDecimal.ZERO);
            result.setTotalCost(BigDecimal.ZERO);
            result.setVan(BigDecimal.ZERO);
            result.setTir(BigDecimal.ZERO);
            result.setTcea(BigDecimal.ZERO);
            return result;
        }

        // PASO 8: Construir tabla de amortización y totales
        buildScheduleAndTotals(request, result, loanAmount, monthlyRate, n);

        // PASO 9: VAN, TIR y TCEA a partir de los flujos de caja usando COK mensual
        computeVanTirTcea(result, loanAmount, discountRatePeriod);

        return result;
    }

    // =================== PASO 1 ===================

    private Property getProperty(SimulationRequest request) {
        return propertyQueryService.getById(request.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
    }

    private Client getClient(SimulationRequest request) {
        return clientQueryService.getById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
    }

    // =================== PASO 2: BONO ===================

    private BigDecimal computeBonusAmount(BigDecimal monthlyIncome,
                                          BigDecimal propertySize,
                                          BigDecimal propertyPrice,
                                          String bonusType) {
        if (bonusType == null) return BigDecimal.ZERO;

        String type = bonusType.trim().toUpperCase();

        BigDecimal incomeAVNMax = new BigDecimal("3715");
        BigDecimal incomeCSP_MV_Max = new BigDecimal("2706");
        BigDecimal maxSizeAVN = new BigDecimal("140");
        BigDecimal sizeLimitSmall = new BigDecimal("50");
        BigDecimal priceLimit1 = new BigDecimal("60000");
        BigDecimal priceLimit2 = new BigDecimal("70000");
        BigDecimal priceLimit3 = new BigDecimal("109000");
        BigDecimal priceLimit4 = new BigDecimal("136000");

        switch (type) {
            case "AVN":
                if (monthlyIncome.compareTo(incomeAVNMax) >= 0) return BigDecimal.ZERO;
                if (propertySize.compareTo(maxSizeAVN) > 0) return BigDecimal.ZERO;

                if (propertySize.compareTo(sizeLimitSmall) <= 0) {
                    return new BigDecimal("46545");
                } else {
                    if (propertyPrice.compareTo(priceLimit1) <= 0) {
                        return new BigDecimal("56710");
                    } else if (propertyPrice.compareTo(priceLimit2) <= 0) {
                        return new BigDecimal("51895");
                    } else if (propertyPrice.compareTo(priceLimit3) <= 0) {
                        return new BigDecimal("50825");
                    } else if (propertyPrice.compareTo(priceLimit4) <= 0) {
                        return new BigDecimal("46545");
                    } else {
                        return BigDecimal.ZERO;
                    }
                }

            case "CSP":
                if (monthlyIncome.compareTo(incomeCSP_MV_Max) < 0) {
                    return new BigDecimal("32100");
                } else {
                    return BigDecimal.ZERO;
                }

            case "MV":
                if (monthlyIncome.compareTo(incomeCSP_MV_Max) < 0) {
                    return new BigDecimal("12305");
                } else {
                    return BigDecimal.ZERO;
                }

            default:
                return BigDecimal.ZERO;
        }
    }

    // =================== PASO 3 ===================

    /**
     * initialPayment llega como porcentaje (ej. 10 = 10%)
     * Saldo a financiar = price - inicial - bono
     */
    private BigDecimal computeSaldoAFinanciar(BigDecimal price,
                                              BigDecimal initialPercent,
                                              BigDecimal bonusAmount) {
        BigDecimal percent = nvl(initialPercent); // 10, 20, etc.
        BigDecimal hundred = BigDecimal.valueOf(100);

        // monto de cuota inicial = price * (percent / 100)
        BigDecimal initialAmount = price
                .multiply(percent)
                .divide(hundred, 2, RoundingMode.HALF_UP);

        BigDecimal saldoAFinanciar = price
                .subtract(initialAmount)
                .subtract(nvl(bonusAmount));

        if (saldoAFinanciar.compareTo(BigDecimal.ZERO) < 0) {
            saldoAFinanciar = BigDecimal.ZERO;
        }
        return saldoAFinanciar;
    }

    private int getValidatedTermMonths(SimulationRequest request) {
        Integer n = request.getTermMonths();
        if (n == null || n <= 0) {
            throw new IllegalArgumentException("termMonths must be > 0");
        }
        return n;
    }

    // =================== PASO 4 ===================

    /**
     * rate se interpreta como tasa anual (TEA / TNA / TET / TNT, etc.) según rateType.
     * Devuelve siempre tasa efectiva mensual (TEM).
     */
    private BigDecimal computeMonthlyRate(BigDecimal rate, String rateType) {
        if (rate == null || rateType == null) return BigDecimal.ZERO;

        String t = rateType.trim().toUpperCase();

        switch (t) {
            // Tasas efectivas
            case "TEM": // ya es tasa efectiva mensual
                return rate;

            case "TEA": // anual -> 12 meses
                return effectiveToMonthly(rate, 12);

            case "TET": // trimestral -> 3 meses
                return effectiveToMonthly(rate, 3);

            case "TES": // semestral -> 6 meses
                return effectiveToMonthly(rate, 6);

            case "TEB": // bimestral -> 2 meses
                return effectiveToMonthly(rate, 2);

            // Tasas nominales
            case "TNA": // nominal anual capitalizable mensual: 12 periodos de 1 mes
                return nominalToMonthly(rate, 12, 1);

            case "TNT": // nominal trimestral: 4 periodos de 3 meses
                return nominalToMonthly(rate, 4, 3);

            case "TNS": // nominal semestral: 2 periodos de 6 meses
                return nominalToMonthly(rate, 2, 6);

            case "TNB": // nominal bimestral: 6 periodos de 2 meses
                return nominalToMonthly(rate, 6, 2);

            default:
                // fallback sencillo: asumimos nominal anual con capitalización mensual
                return rate
                        .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal effectiveToMonthly(BigDecimal rate, int monthsPerPeriod) {
        double r = rate.doubleValue();
        double tem = Math.pow(1 + r, 1.0 / monthsPerPeriod) - 1.0;
        return BigDecimal.valueOf(tem).setScale(10, RoundingMode.HALF_UP);
    }

    private BigDecimal nominalToMonthly(BigDecimal rate, int periodsPerYear, int monthsPerPeriod) {
        double iNom = rate.doubleValue();
        double iPer = iNom / periodsPerYear; // tasa por periodo de capitalización
        double tem = Math.pow(1 + iPer, 1.0 / monthsPerPeriod) - 1.0;
        return BigDecimal.valueOf(tem).setScale(10, RoundingMode.HALF_UP);
    }

    // =================== PASO 5 ===================

    /**
     * Método francés: cuota fija
     */
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

    // =================== PASO 6 ===================

    /**
     * Suma de costos iniciales y periódicos según request.
     * Para los totales desagregados (seguros, comisiones, portes) se multiplica
     * amount por el número de periodos en que aplican.
     */
    private void computeCostTotals(SimulationRequest request,
                                   SimulationResult result,
                                   int totalPeriods) {

        BigDecimal totalInitialCosts = BigDecimal.ZERO;
        BigDecimal totalPeriodicConfig = BigDecimal.ZERO;

        BigDecimal totalLifeInsurance = BigDecimal.ZERO;
        BigDecimal totalRiskInsurance = BigDecimal.ZERO;
        BigDecimal totalPeriodicCommissions = BigDecimal.ZERO;
        BigDecimal totalPortes = BigDecimal.ZERO;

        if (request.getCosts() != null) {
            for (SimulationRequest.CostItem item : request.getCosts()) {
                if (item.getAmount() == null) continue;

                if (item.getType() == CostType.INITIAL) {
                    totalInitialCosts = totalInitialCosts.add(item.getAmount());
                } else if (item.getType() == CostType.PERIODIC) {
                    totalPeriodicConfig = totalPeriodicConfig.add(item.getAmount());

                    int occurrences = (item.getPeriodNumber() == null) ? totalPeriods : 1;
                    BigDecimal totalForItem = item.getAmount()
                            .multiply(BigDecimal.valueOf(occurrences));

                    String code = item.getCode() != null
                            ? item.getCode().trim().toUpperCase()
                            : "";

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

    // =================== PASO 7 ===================

    private void buildScheduleAndTotals(
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

        if ("NINGUNA".equals(graceType) || graceMonths <= 0) {
            // Sin gracia: método francés normal
            BigDecimal monthlyInstallment = computeInstallment(loanAmount, monthlyRate, n);
            result.setMonthlyInstallment(monthlyInstallment);

            for (int period = 1; period <= n; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, period);
                BigDecimal totalPeriodCost = monthlyInstallment.add(periodCosts);

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
        } else if ("PARCIAL".equals(graceType)) {
            // Gracia PARCIAL: primeros meses se paga solo interés; luego método francés
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
        } else if ("TOTAL".equals(graceType)) {
            // Gracia TOTAL: durante la gracia no se paga, intereses se capitalizan al saldo.
            int nEffective = n - graceMonths;
            if (nEffective <= 0) {
                nEffective = n;
                graceMonths = 0;
            }

            // Fase 1: meses de gracia (capitalización de intereses)
            for (int period = 1; period <= graceMonths; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = BigDecimal.ZERO;
                BigDecimal installment = BigDecimal.ZERO; // no se paga nada de cuota

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, period);
                BigDecimal totalPeriodCost = installment.add(periodCosts);

                // Intereses se capitalizan
                BigDecimal endingBalance = balance.add(interest);

                totalInterest = totalInterest.add(interest);
                // principal = 0
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
                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, p);
                BigDecimal totalPeriodCost = monthlyInstallment.add(periodCosts);

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
        } else {
            // Cualquier otra cosa, lo tratamos como sin gracia (francés normal)
            BigDecimal monthlyInstallment = computeInstallment(loanAmount, monthlyRate, n);
            result.setMonthlyInstallment(monthlyInstallment);

            for (int period = 1; period <= n; period++) {
                SimulationResult.ScheduleItem row = new SimulationResult.ScheduleItem();
                row.setPeriod(period);
                row.setBeginningBalance(balance.setScale(2, RoundingMode.HALF_UP));

                BigDecimal interest = balance.multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal principal = monthlyInstallment.subtract(interest)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal periodCosts = computePeriodicCostsForPeriod(request, period);
                BigDecimal totalPeriodCost = monthlyInstallment.add(periodCosts);

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

    // =================== PASO 8: VAN / TIR / TCEA ===================

    private void computeVanTirTcea(SimulationResult result,
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

    // =================== Utilidades ===================

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
