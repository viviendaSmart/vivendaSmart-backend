package com.firststudent.platform.viviendasmartbackend.credit.interfaces.rest.transform;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.aggregates.Credit;
import com.firststudent.platform.viviendasmartbackend.credit.interfaces.rest.resources.CreditResource;

/**
 * Assembler para convertir entidades Credit a recursos REST
 */
public class CreditResourceFromEntityAssembler {
    public static CreditResource toResourceFromEntity(Credit entity) {
        return new CreditResource(
            entity.getId(),
            entity.getFundedAmount(),
            entity.getCurrency(),
            entity.getRateType(),
            entity.getAnnualRate(),
            entity.getMonthlyRate(),
            entity.getTermMonths(),
            entity.getGraceType(),
            entity.getGraceMonths(),
            entity.getBonusId(),
            entity.getFixedInstallment(),
            entity.getTotalInterest(),
            entity.getTotalAmountPaid(),
            entity.getVAN(),
            entity.getTIR(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

