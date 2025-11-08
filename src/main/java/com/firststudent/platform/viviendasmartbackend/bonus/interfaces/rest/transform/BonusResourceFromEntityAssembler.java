package com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.transform;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;
import com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.resources.BonusResource;

/**
 * Assembler para convertir entidades Bonus a recursos REST
 */
public class BonusResourceFromEntityAssembler {
    public static BonusResource toResourceFromEntity(Bonus entity) {
        return new BonusResource(
            entity.getId(),
            entity.getName(),
            entity.getAmount(),
            entity.getRequirements(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

