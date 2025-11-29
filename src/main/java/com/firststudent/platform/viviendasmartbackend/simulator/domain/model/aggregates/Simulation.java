package com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates;

import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.valueobjects.CostCalcMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulations")
@Getter
@Setter
@NoArgsConstructor
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ====== Metadata / relaciones simples (por id) ======
    private Long userId;
    private Long clientId;
    private Long propertyId;

    private LocalDateTime createdAt;

    // ====== Inputs principales de la simulación ======
    // (snapshot de lo que te mandó el front + lo que vino de Config)

    /** Moneda: "PEN" o "USD" */
    private String currency;

    /** Precio de la propiedad al momento de simular (snapshot) */
    private BigDecimal propertyPrice;

    /** % de cuota inicial */
    private BigDecimal initialPaymentPercent;

    /** Plazo en años (lo puedes guardar también en meses si quieres) */
    private Integer termYears;

    /** Frecuencia de pago en días: 30, 60, 90, etc. */
    private Integer frequencyDays;

    /** Tasa del préstamo (snapshot de Config) */
    private BigDecimal rate;
    private String rateType;   // "TEA", "TNA", etc.

    /** Datos del COK */
    private BigDecimal cokRate;
    private String cokRateType;

    /** Bono usado: AVN / CSP / MV / null */
    private String bonusType;

    /** Tipo y periodo de gracia (si lo usas) */
    private String graceType;      // "TOTAL", "PARCIAL", "NINGUNA"
    private Integer graceMonths;

    // ====== Resultados agregados (resumen del SimulationResult) ======

    // Saldo a financiar del activo
    private BigDecimal financedBalance;

    // Bono aplicado
    private BigDecimal bonusAmount;

    // Monto del préstamo
    private BigDecimal loanAmount;

    // Tasa efectiva por periodo
    private BigDecimal periodRate;

    // Cuota fija
    private BigDecimal installment;

    // Nº total de cuotas y cuotas por año
    private Integer totalTerm;
    private Integer installmentsPerYear;

    // Tasas de seguros por periodo
    private BigDecimal lifeInsuranceRatePeriod;
    private BigDecimal riskInsuranceRatePeriod;

    // Totales
    private BigDecimal totalInterest;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalPrincipalAmortization;

    private BigDecimal totalLifeInsurance;
    private BigDecimal totalRiskInsurance;
    private BigDecimal totalPeriodicCommissions;
    private BigDecimal totalPortes;

    private BigDecimal totalInitialCosts;
    private BigDecimal totalPeriodicCosts;
    private BigDecimal totalCost;

    // Indicadores financieros
    private BigDecimal discountRatePeriod;
    private BigDecimal van;
    private BigDecimal tir;
    private BigDecimal tcea;

    // ====== Costos detallados de entrada ======
    @ElementCollection
    @CollectionTable(
            name = "simulation_costs",
            joinColumns = @JoinColumn(name = "simulation_id")
    )
    private List<SimulationCostItem> costs = new ArrayList<>();

    // ====== Tabla de amortización ======
    @OneToMany(
            mappedBy = "simulation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SimulationScheduleItem> schedule = new ArrayList<>();

    // ====== Helpers de dominio ======

    public void addScheduleItem(SimulationScheduleItem item) {
        schedule.add(item);
        item.setSimulation(this);
    }

    public void addCostItem(SimulationCostItem costItem) {
        this.costs.add(costItem);
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
