package com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.transform;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;
import com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.resources.ConfigResource;


public class ConfigResourceFromEntityAssembler {
    public static ConfigResource toResourceFromEntity(Config entity) {
        return new ConfigResource(
                entity.getId(),
                entity.getRate(),
                entity.getRateType(),
                entity.getExchange(),
                entity.getTermType(),
                entity.getTerm(),
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
