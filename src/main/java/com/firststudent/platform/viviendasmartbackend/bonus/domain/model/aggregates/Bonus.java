package com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates;

import java.math.BigDecimal;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.valueobjects.BonusType;
import com.firststudent.platform.viviendasmartbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Agregado Bono
 * Representa un bono disponible en el sistema con su monto y requisitos.
 */
@Getter
@Entity
public class Bonus extends AuditableAbstractAggregateRoot<Bonus> {

    @Column
    @Enumerated(EnumType.STRING)
    private BonusType bonusType;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal amount;

    @NotNull
    @Column(nullable = false)
    private Long creditId;

    protected Bonus() {
    }

    public Bonus(BonusType bonusType, BigDecimal amount, Long creditId) {
        this.bonusType = bonusType;
        this.amount = amount;
        this.creditId = creditId;
    }

    /**
     * Actualiza los detalles del bono
     */
    public void updateDetails(BonusType bonusType, BigDecimal amount) {
        if (bonusType != null) {
            this.bonusType = bonusType;
        }
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.amount = amount;
        }
    }
}

