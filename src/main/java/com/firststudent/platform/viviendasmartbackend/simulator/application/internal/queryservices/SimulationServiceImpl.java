package com.firststudent.platform.viviendasmartbackend.simulator.application.internal.queryservices;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientQueryService;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.GraceType;
import com.firststudent.platform.viviendasmartbackend.config.domain.services.ConfigQueryService;
import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.domain.services.PropertyQueryService;
import com.firststudent.platform.viviendasmartbackend.simulator.application.internal.SimulationMapper;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates.Simulation;
import com.firststudent.platform.viviendasmartbackend.simulator.infraestructure.persistence.jpa.repositories.SimulationRepository;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.BonusCalculator;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.CostTotalsCalculator;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.FinancialIndicatorsCalculator;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.RateConverter;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.ScheduleGenerator;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.SimulationService;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SimulationServiceImpl implements SimulationService {

    private final PropertyQueryService propertyQueryService;
    private final ClientQueryService clientQueryService;
    private final ConfigQueryService configQueryService;
    private final BonusCalculator bonusCalculator;
    private final CostTotalsCalculator costTotalsCalculator;
    private final RateConverter rateConverter;
    private final ScheduleGenerator scheduleGenerator;
    private final FinancialIndicatorsCalculator financialIndicatorsCalculator;
    private final SimulationRepository simulationRepository;

    public SimulationServiceImpl(PropertyQueryService propertyQueryService,
                                 ClientQueryService clientQueryService,
                                 ConfigQueryService configQueryService,
                                 BonusCalculator bonusCalculator,
                                 CostTotalsCalculator costTotalsCalculator,
                                 RateConverter rateConverter,
                                 ScheduleGenerator scheduleGenerator,
                                 FinancialIndicatorsCalculator financialIndicatorsCalculator,
                                 SimulationRepository simulationRepository) {
        this.propertyQueryService = propertyQueryService;
        this.clientQueryService = clientQueryService;
        this.configQueryService = configQueryService;
        this.bonusCalculator = bonusCalculator;
        this.costTotalsCalculator = costTotalsCalculator;
        this.rateConverter = rateConverter;
        this.scheduleGenerator = scheduleGenerator;
        this.financialIndicatorsCalculator = financialIndicatorsCalculator;
        this.simulationRepository = simulationRepository;
    }

    @Override
    @Transactional
    public SimulationResult simulate(SimulationRequest request) {

        // ========= PASO 0: Obtener Config por userId =========
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required for simulation");
        }

        Config config = configQueryService.getByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Config not found for userId " + request.getUserId()));

        BigDecimal configRate = nvl(config.getRate()); // TEA, TNA, etc. según rateType
        String configRateType = (config.getRateType() != null)
                ? config.getRateType().name()
                : null;

        String exchange = (config.getExchange() != null)
                ? config.getExchange().name()   // "PEN" o "USD"
                : "PEN";

        // ====== Grace desde Config (lo usaremos en ambos caminos) ======
        GraceType graceType = config.getGraceType();
        Integer graceTermMonths = config.getTerm();

        String graceTypeCode = (graceType != null) ? graceType.name() : "NINGUNA";
        int graceMonths = (graceTermMonths != null) ? graceTermMonths : 0;

        // ========= PASO 1: Propiedad e ingresos =========
        Property property = getProperty(request);
        BigDecimal price = nvl(property.getPrice());
        BigDecimal propertySize = nvl(property.getSize());

        Client client = getClient(request);
        BigDecimal monthlyIncome = nvl(client.getMonthlyIncome());

        // ========= PASO 2: Bono (AVN, CSP, MV) =========
        BigDecimal bonusAmount = bonusCalculator.calculate(
                monthlyIncome,
                propertySize,
                price,
                request.getBonusType()
        );

        // ========= PASO 3: Saldo a financiar =========
        BigDecimal financedBalance = computeSaldoAFinanciar(
                price,
                request.getInitialPayment(),
                bonusAmount
        );

        // ========= PASO 4: Frecuencia y plazo =========
        int installmentsPerYear = getInstallmentsPerYear(request);                // 360 / frequency
        int totalTerm = getTotalTermInInstallments(request, installmentsPerYear); // termYears * installmentsPerYear

        SimulationResult result = new SimulationResult();
        result.setFinancedBalance(financedBalance);
        result.setBonusAmount(bonusAmount);
        result.setTotalTerm(totalTerm);
        result.setInstallmentsPerYear(installmentsPerYear);

        // Precio de la propiedad para seguro de riesgo
        result.setPropertyPrice(price);

        // ========= PASO 5: Tasas de seguros por periodo =========
        Integer freqDays = request.getFrequency();
        BigDecimal freqBD = BigDecimal.valueOf(freqDays);

        BigDecimal lifeInsurancePercent = getInsurancePercentage(request, "SEGURO_DESGRAVAMEN");
        BigDecimal riskInsurancePercent = getInsurancePercentage(request, "SEGURO_RIESGO");

        BigDecimal lifeInsuranceRatePeriod = lifeInsurancePercent
                .multiply(freqBD)
                .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal riskInsuranceRatePeriod = riskInsurancePercent
                .multiply(freqBD)
                .divide(BigDecimal.valueOf(360), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        result.setLifeInsuranceRatePeriod(lifeInsuranceRatePeriod);
        result.setRiskInsuranceRatePeriod(riskInsuranceRatePeriod);

        // ========= PASO 6: Costos iniciales y periódicos (en soles) =========
        costTotalsCalculator.computeCostTotals(request, result, financedBalance, totalTerm);

        BigDecimal totalInitialCosts = nvl(result.getTotalInitialCosts());

        // ========= PASO 7: Monto del préstamo =========
        BigDecimal loanAmount = financedBalance.add(totalInitialCosts);
        if (loanAmount.compareTo(BigDecimal.ZERO) < 0) {
            loanAmount = BigDecimal.ZERO;
        }
        result.setLoanAmount(loanAmount);

        // ========= PASO 8: Tasa efectiva mensual (desde Config) =========
        BigDecimal monthlyRate = rateConverter.toMonthly(
                configRate,
                configRateType
        );
        result.setMonthlyRate(monthlyRate);

        // ========= PASO 9: COK (desde request) =========
        BigDecimal discountRateYearly = rateConverter.toYearly(
                request.getCokRate(),
                request.getCokRateType()
        );

        BigDecimal discountRatePeriod = rateConverter.yearlyToPeriod(
                discountRateYearly,
                freqDays
        );
        result.setDiscountRatePeriod(discountRatePeriod);

        // ========= CASO ESPECIAL: loanAmount == 0 =========
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
            result.setSchedule(new java.util.ArrayList<>());

            result.setCurrency(exchange);

            // Persistimos también este caso
            persistSimulation(
                    request,
                    result,
                    configRate,
                    configRateType,
                    graceTypeCode,
                    graceMonths
            );

            return result;
        }

        // ========= PASO 10: Tabla de amortización y flujos =========
        scheduleGenerator.buildScheduleAndTotals(
                request,
                result,
                loanAmount,
                monthlyRate,
                totalTerm,
                graceTypeCode,
                graceMonths
        );

        // ========= PASO 11: VAN, TIR, TCEA =========
        financialIndicatorsCalculator.computeVanTirTcea(
                result,
                loanAmount,
                discountRatePeriod
        );

        // ========= PASO 12: Conversión de moneda según Config.exchange =========
        applyExchangeConversionIfNeeded(result, exchange);

        // Guardamos en qué moneda finalmente están los montos
        result.setCurrency(exchange);

        // ========= PASO 13: Persistir el aggregate Simulation =========
        persistSimulation(
                request,
                result,
                configRate,
                configRateType,
                graceTypeCode,
                graceMonths
        );

        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Simulation> getAllSimulations() {
        return simulationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Simulation> getSimulationsByUserId(Long userId) {
        return simulationRepository.findByUserId(userId);
    }



    // =================== Helpers internos ===================

    private Property getProperty(SimulationRequest request) {
        return propertyQueryService.getById(request.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
    }

    private Client getClient(SimulationRequest request) {
        return clientQueryService.getById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
    }

    /**
     * initialPayment llega como porcentaje (ej. 10 = 10%)
     * Saldo a financiar = price - inicial - bono
     */
    private BigDecimal computeSaldoAFinanciar(BigDecimal price,
                                              BigDecimal initialPercent,
                                              BigDecimal bonusAmount) {
        BigDecimal percent = nvl(initialPercent); // 10, 20, etc.
        BigDecimal hundred = BigDecimal.valueOf(100);

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

    /**
     * installmentsPerYear = 360 / frequency
     */
    private int getInstallmentsPerYear(SimulationRequest request) {
        Integer frequency = request.getFrequency();
        if (frequency == null || frequency <= 0) {
            throw new IllegalArgumentException("frequency must be > 0");
        }

        if (360 % frequency != 0) {
            throw new IllegalArgumentException("frequency must be a divisor of 360 (received: " + frequency + ")");
        }

        return 360 / frequency;
    }

    /**
     * totalTerm = termYears * installmentsPerYear
     */
    private int getTotalTermInInstallments(SimulationRequest request, int installmentsPerYear) {
        Integer termYears = request.getTermYears();
        if (termYears == null || termYears <= 0) {
            throw new IllegalArgumentException("termYears must be > 0");
        }
        return termYears * installmentsPerYear;
    }

    /**
     * Busca el primer costo periódico en modo PERCENTAGE con el código indicado
     * y devuelve su amount (o 0 si no existe).
     */
    private BigDecimal getInsurancePercentage(SimulationRequest request, String code) {
        if (request.getCosts() == null) return BigDecimal.ZERO;

        return request.getCosts().stream()
                .filter(c -> c.getCode() != null && c.getCode().equalsIgnoreCase(code))
                .filter(c -> c.getCalcMode() == SimulationRequest.CostCalcMode.PERCENTAGE)
                .map(c -> nvl(c.getAmount()))
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    // =================== Conversión de moneda ===================

    /**
     * Convierte todos los montos de S/ a US$ si exchange = "USD".
     * No modifica tasas (monthlyRate, TIR, TCEA, etc.).
     */
    private void applyExchangeConversionIfNeeded(SimulationResult result, String exchange) {
        // Ajusta estos if según cómo tengas los enums de moneda;
        // aquí asumo "PEN" y "USD".
        if (exchange == null || exchange.equalsIgnoreCase("PEN")) {
            return; // ya está en soles
        }

        if (!exchange.equalsIgnoreCase("USD")) {
            // Por ahora solo manejamos PEN y USD; otros casos puedes implementarlos luego
            return;
        }

        BigDecimal fx = getExchangeRateForUsd();
        if (fx == null || fx.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Campos principales
        result.setFinancedBalance(convertAmount(result.getFinancedBalance(), fx));
        result.setBonusAmount(convertAmount(result.getBonusAmount(), fx));
        result.setLoanAmount(convertAmount(result.getLoanAmount(), fx));
        result.setMonthlyInstallment(convertAmount(result.getMonthlyInstallment(), fx));

        result.setTotalInterest(convertAmount(result.getTotalInterest(), fx));
        result.setTotalAmountPaid(convertAmount(result.getTotalAmountPaid(), fx));
        result.setTotalPrincipalAmortization(convertAmount(result.getTotalPrincipalAmortization(), fx));

        result.setTotalLifeInsurance(convertAmount(result.getTotalLifeInsurance(), fx));
        result.setTotalRiskInsurance(convertAmount(result.getTotalRiskInsurance(), fx));
        result.setTotalPeriodicCommissions(convertAmount(result.getTotalPeriodicCommissions(), fx));
        result.setTotalPortes(convertAmount(result.getTotalPortes(), fx));

        result.setTotalInitialCosts(convertAmount(result.getTotalInitialCosts(), fx));
        result.setTotalPeriodicCosts(convertAmount(result.getTotalPeriodicCosts(), fx));
        result.setTotalCost(convertAmount(result.getTotalCost(), fx));
        result.setPropertyPrice(convertAmount(result.getPropertyPrice(), fx));

        result.setVan(convertAmount(result.getVan(), fx));
        // TIR, TCEA y discountRatePeriod NO se modifican

        // Detalle de la tabla
        if (result.getSchedule() != null) {
            for (SimulationResult.ScheduleItem row : result.getSchedule()) {
                row.setBeginningBalance(convertAmount(row.getBeginningBalance(), fx));
                row.setInstallment(convertAmount(row.getInstallment(), fx));
                row.setInterest(convertAmount(row.getInterest(), fx));
                row.setPrincipal(convertAmount(row.getPrincipal(), fx));
                row.setCashFlow(convertAmount(row.getCashFlow(), fx));

                row.setLifeInsurance(convertAmount(row.getLifeInsurance(), fx));
                row.setRiskInsurance(convertAmount(row.getRiskInsurance(), fx));
                row.setPeriodicCommission(convertAmount(row.getPeriodicCommission(), fx));

                row.setPeriodicCosts(convertAmount(row.getPeriodicCosts(), fx));
                row.setTotalPeriodCost(convertAmount(row.getTotalPeriodCost(), fx));
                row.setEndingBalance(convertAmount(row.getEndingBalance(), fx));
            }
        }
    }

    private BigDecimal convertAmount(BigDecimal value, BigDecimal fx) {
        if (value == null) return null;
        return value.divide(fx, 2, RoundingMode.HALF_UP);
    }

    /**
     * Tipo de cambio fijo de ejemplo.
     * Luego lo puedes sacar de otra tabla o de la propia Config si la amplías.
     */
    private BigDecimal getExchangeRateForUsd() {
        return new BigDecimal("3.4"); // ej: 1 USD = 3.40 PEN
    }


    // =================== Persistencia del aggregate ===================

    private void persistSimulation(
            SimulationRequest request,
            SimulationResult result,
            BigDecimal configRate,
            String configRateType,
            String graceTypeCode,
            Integer graceMonths
    ) {
        Simulation simulation = SimulationMapper.toAggregate(
                request, result,
                configRate, configRateType,
                graceTypeCode, graceMonths
        );
        simulationRepository.save(simulation);
    }
}
