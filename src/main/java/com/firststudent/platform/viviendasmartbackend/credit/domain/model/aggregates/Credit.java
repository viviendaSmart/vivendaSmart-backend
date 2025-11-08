package com.firststudent.platform.viviendasmartbackend.credit.domain.model.aggregates;

import java.math.BigDecimal;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.valueobjects.GraceType;
import com.firststudent.platform.viviendasmartbackend.credit.domain.model.valueobjects.RateType;
import com.firststudent.platform.viviendasmartbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Agregado Crédito
 * Representa un crédito en el sistema con toda su información financiera.
 * Incluye cálculos para el método francés (VAN, TIR, cuota fija, etc.).
 */
@Getter
@Entity
public class Credit extends AuditableAbstractAggregateRoot<Credit> {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fundedAmount;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RateType rateType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal annualRate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal monthlyRate;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer termMonths;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GraceType graceType;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer graceMonths;

    // Referencia al Bono por ID (puede ser null si no hay bono aplicado)
    @Column(nullable = true)
    private Long bonusId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fixedInstallment;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterest;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmountPaid;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal VAN;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal TIR;

    protected Credit() {
    }

    /**
     * Constructor para crear un nuevo crédito
     * @param fundedAmount el monto financiado
     * @param currency la moneda
     * @param rateType el tipo de tasa
     * @param annualRate la tasa anual
     * @param monthlyRate la tasa mensual
     * @param termMonths el plazo en meses
     * @param graceType el tipo de período de gracia
     * @param graceMonths los meses de gracia
     * @param bonusId el ID del bono aplicado (puede ser null)
     * @param fixedInstallment la cuota fija calculada
     * @param totalInterest el interés total
     * @param totalAmountPaid el monto total pagado
     * @param VAN el Valor Actual Neto
     * @param TIR la Tasa Interna de Retorno
     */
    public Credit(BigDecimal fundedAmount, String currency, RateType rateType,
                  BigDecimal annualRate, BigDecimal monthlyRate, Integer termMonths,
                  GraceType graceType, Integer graceMonths, Long bonusId,
                  BigDecimal fixedInstallment, BigDecimal totalInterest,
                  BigDecimal totalAmountPaid, BigDecimal VAN, BigDecimal TIR) {
        this.fundedAmount = fundedAmount;
        this.currency = currency;
        this.rateType = rateType;
        this.annualRate = annualRate;
        this.monthlyRate = monthlyRate;
        this.termMonths = termMonths;
        this.graceType = graceType;
        this.graceMonths = graceMonths;
        this.bonusId = bonusId;
        this.fixedInstallment = fixedInstallment;
        this.totalInterest = totalInterest;
        this.totalAmountPaid = totalAmountPaid;
        this.VAN = VAN;
        this.TIR = TIR;
    }

    /**
     * Actualiza los cálculos del crédito
     */
    public void updateCalculations(BigDecimal fixedInstallment, BigDecimal totalInterest,
                                  BigDecimal totalAmountPaid, BigDecimal VAN, BigDecimal TIR) {
        if (fixedInstallment != null) {
            this.fixedInstallment = fixedInstallment;
        }
        if (totalInterest != null) {
            this.totalInterest = totalInterest;
        }
        if (totalAmountPaid != null) {
            this.totalAmountPaid = totalAmountPaid;
        }
        if (VAN != null) {
            this.VAN = VAN;
        }
        if (TIR != null) {
            this.TIR = TIR;
        }
    }
}

