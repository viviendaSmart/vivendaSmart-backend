package com.firststudent.platform.viviendasmartbackend.credit.domain.services;

import java.math.BigDecimal;
import java.util.Optional;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.aggregates.Credit;
import com.firststudent.platform.viviendasmartbackend.credit.domain.model.valueobjects.GraceType;
import com.firststudent.platform.viviendasmartbackend.credit.domain.model.valueobjects.RateType;

public interface CreditCommandService {
    /**
     * Crea un nuevo crédito
     */
    Credit create(BigDecimal fundedAmount, String currency, RateType rateType,
                 BigDecimal annualRate, BigDecimal monthlyRate, Integer termMonths,
                 GraceType graceType, Integer graceMonths, Long bonusId,
                 BigDecimal fixedInstallment, BigDecimal totalInterest,
                 BigDecimal totalAmountPaid, BigDecimal VAN, BigDecimal TIR);
    
    /**
     * Actualiza los cálculos de un crédito existente
     */
    Optional<Credit> updateCalculations(Long creditId, BigDecimal fixedInstallment,
                                      BigDecimal totalInterest, BigDecimal totalAmountPaid,
                                      BigDecimal VAN, BigDecimal TIR);
    
    /**
     * Elimina un crédito
     */
    void delete(Long creditId);
}

