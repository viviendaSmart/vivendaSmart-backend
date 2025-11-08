package com.firststudent.platform.viviendasmartbackend.credit.interfaces.rest.resources;

import java.math.BigDecimal;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.valueobjects.GraceType;
import com.firststudent.platform.viviendasmartbackend.credit.domain.model.valueobjects.RateType;

/**
 * Recurso para crear un nuevo crédito
 */
public record CreateCreditResource(
    BigDecimal fundedAmount,
    String currency,
    RateType rateType,
    BigDecimal annualRate,
    BigDecimal monthlyRate,
    Integer termMonths,
    GraceType graceType,
    Integer graceMonths,
    Long bonusId,
    BigDecimal fixedInstallment,
    BigDecimal totalInterest,
    BigDecimal totalAmountPaid,
    BigDecimal VAN,
    BigDecimal TIR
) {}

