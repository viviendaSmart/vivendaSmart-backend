package com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates;

import java.math.BigDecimal;

import com.firststudent.platform.viviendasmartbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Agregado Bono
 * Representa un bono disponible en el sistema con su monto y requisitos.
 */
@Getter
@Entity
public class Bonus extends AuditableAbstractAggregateRoot<Bonus> {

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String requirements;

    protected Bonus() {
    }

    /**
     * Constructor para crear un nuevo bono
     * @param name el nombre del bono
     * @param amount el monto del bono
     * @param requirements los requisitos para obtener el bono
     */
    public Bonus(String name, BigDecimal amount, String requirements) {
        this.name = name;
        this.amount = amount;
        this.requirements = requirements;
    }

    /**
     * Actualiza los detalles del bono
     */
    public void updateDetails(String name, BigDecimal amount, String requirements) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.amount = amount;
        }
        if (requirements != null && !requirements.isBlank()) {
            this.requirements = requirements;
        }
    }
}

