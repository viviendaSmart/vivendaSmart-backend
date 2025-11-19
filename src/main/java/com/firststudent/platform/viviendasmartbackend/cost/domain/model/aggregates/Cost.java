package com.firststudent.platform.viviendasmartbackend.cost.domain.model.aggregates;

import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;
import com.firststudent.platform.viviendasmartbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
public class Cost extends AuditableAbstractAggregateRoot<Cost> {

    @NotNull
    @Column(nullable = false)
    private Long creditId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CostType costType; // INITIAL, PERIODIC

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;

    @Column
    private Integer periodNumber;

    protected Cost(){}

    public Cost(CostType costType, BigDecimal amount, Integer periodNumber, Long creditId ) {
        this.creditId = creditId;
        this.costType = costType;
        this.amount = amount;
        this.periodNumber = periodNumber;
    }

    public void updateDetails(CostType costType, BigDecimal amount, Integer periodNumber) {
        if (costType != null) {
            this.costType = costType;
        }
        if (amount != null && amount.compareTo(BigDecimal.ZERO) >= 0) {
            this.amount = amount;
        }
        if (periodNumber != null) {
            this.periodNumber = periodNumber;
        }
    }
}