package com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "simulation_schedule_items")
@Getter
@Setter
@NoArgsConstructor
public class SimulationScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el aggregate root
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id")
    @JsonIgnore
    private Simulation simulation;

    private Integer period;            // Nº
    private String graceFlag;          // "T", "P" o ""

    private BigDecimal beginningBalance;
    private BigDecimal installment;
    private BigDecimal interest;
    private BigDecimal principal;
    private BigDecimal cashFlow;

    private BigDecimal lifeInsurance;
    private BigDecimal riskInsurance;
    private BigDecimal periodicCommission;

    private BigDecimal periodicCosts;
    private BigDecimal totalPeriodCost;
    private BigDecimal endingBalance;
}
