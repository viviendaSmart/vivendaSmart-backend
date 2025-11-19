package com.firststudent.platform.viviendasmartbackend.cost.interfaces.rest.transform;


import com.firststudent.platform.viviendasmartbackend.cost.domain.model.aggregates.Cost;
import com.firststudent.platform.viviendasmartbackend.cost.interfaces.rest.resources.CostResource;

public class CostResourceFromEntityAssembler {
    public static CostResource toResourceFromEntity(Cost entity) {
        return new CostResource(
                entity.getId(),
                entity.getCostType(),
                entity.getAmount(),
                entity.getPeriodNumber(),
                entity.getCreditId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
