package com.firststudent.platform.viviendasmartbackend.simulator.application.internal.queryservices;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientQueryService;
import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.domain.services.PropertyQueryService;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.BonusCalculator;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.CostTotalsCalculator;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.FinancialIndicatorsCalculator;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.RateConverter;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.ScheduleGenerator;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.SimulationService;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SimulationServiceImpl implements SimulationService {

    private final PropertyQueryService propertyQueryService;
    private final ClientQueryService clientQueryService;

    private final BonusCalculator bonusCalculator;
    private final CostTotalsCalculator costTotalsCalculator;
    private final RateConverter rateConverter;
    private final ScheduleGenerator scheduleGenerator;
    private final FinancialIndicatorsCalculator financialIndicatorsCalculator;

    public SimulationServiceImpl(PropertyQueryService propertyQueryService,
                                 ClientQueryService clientQueryService,
                                 BonusCalculator bonusCalculator,
                                 CostTotalsCalculator costTotalsCalculator,
                                 RateConverter rateConverter,
                                 ScheduleGenerator scheduleGenerator,
                                 FinancialIndicatorsCalculator financialIndicatorsCalculator) {
        this.propertyQueryService = propertyQueryService;
        this.clientQueryService = clientQueryService;
        this.bonusCalculator = bonusCalculator;
        this.costTotalsCalculator = costTotalsCalculator;
        this.rateConverter = rateConverter;
        this.scheduleGenerator = scheduleGenerator;
        this.financialIndicatorsCalculator = financialIndicatorsCalculator;
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
        BigDecimal bonusAmount = bonusCalculator.calculate(
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

        // ====== FRECUENCIA Y PLAZO EN CUOTAS ======
        int installmentsPerYear = getInstallmentsPerYear(request);              // 360 / frequency
        int totalTerm = getTotalTermInInstallments(request, installmentsPerYear); // termYears * installmentsPerYear

        SimulationResult result = new SimulationResult();
        result.setFinancedBalance(financedBalance);
        result.setBonusAmount(bonusAmount);
        result.setTotalTerm(totalTerm);                       // Nº total de cuotas
        result.setInstallmentsPerYear(installmentsPerYear);   // Nº de cuotas por año

        // ====== TASAS DE SEGUROS POR PERÍODO ======
        Integer freqDays = request.getFrequency();
        BigDecimal freqBD = BigDecimal.valueOf(freqDays);

        // Tasa de seguro desgravamen (% anual o base) tal como viene del request,
        BigDecimal lifeInsurancePercent = getInsurancePercentage(request, "SEGURO_DESGRAVAMEN");

        // Tasa de seguro de riesgo (%) tal como viene del request
        BigDecimal riskInsurancePercent = getInsurancePercentage(request, "SEGURO_RIESGO");

        // lifeInsuranceRatePeriod(%) = %SeguroDesgravamen * frequency / 30
        BigDecimal lifeInsuranceRatePeriod = lifeInsurancePercent
                .multiply(freqBD)
                .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        // riskInsuranceRatePeriod(%) = %SeguroRiesgo * frequency / 360
        BigDecimal riskInsuranceRatePeriod = riskInsurancePercent
                .multiply(freqBD)
                .divide(BigDecimal.valueOf(360), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);


        // Los seteamos en el resultado, también como porcentaje
        result.setLifeInsuranceRatePeriod(lifeInsuranceRatePeriod);
        result.setRiskInsuranceRatePeriod(riskInsuranceRatePeriod);

        // PASO 5: Calcular totales de costos (iniciales + periódicos configurados)
        costTotalsCalculator.computeCostTotals(request, result, financedBalance, totalTerm);

        BigDecimal totalInitialCosts = nvl(result.getTotalInitialCosts());

        // PASO 6: Monto del préstamo = saldo a financiar + costos iniciales
        BigDecimal loanAmount = financedBalance.add(totalInitialCosts);
        if (loanAmount.compareTo(BigDecimal.ZERO) < 0) {
            loanAmount = BigDecimal.ZERO;
        }
        result.setLoanAmount(loanAmount);

        // PASO 7: Tasa efectiva mensual del préstamo
        BigDecimal monthlyRate = rateConverter.toMonthly(
                request.getRate(),
                request.getRateType()
        );
        result.setMonthlyRate(monthlyRate);

        // PASO 7.1: Tasa de descuento a TEA
        BigDecimal discountRateYearly = rateConverter.toYearly(
                request.getCokRate(),
                request.getCokRateType()
        );

        // PASO 7.2: Tasa de descuento anuala a (COK período)
        BigDecimal discountRatePeriod = rateConverter.yearlyToPeriod(
                discountRateYearly,
                freqDays
        );

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
        scheduleGenerator.buildScheduleAndTotals(
                request,
                result,
                loanAmount,
                monthlyRate,
                totalTerm
        );

        // PASO 9: VAN, TIR y TCEA a partir de los flujos de caja usando COK mensual
        financialIndicatorsCalculator.computeVanTirTcea(
                result,
                loanAmount,
                discountRatePeriod
        );

        return result;
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
}
