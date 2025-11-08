package com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.transform;

import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.resources.PropertyResource;

public class PropertyResourceFromEntityAssembler {
    public static PropertyResource toResourceFromEntity(Property entity) {
        return new PropertyResource(
            entity.getId(),
            entity.getAddress(),
            entity.getPrice(),
            entity.getSize(),
            entity.getPhoto(),
            entity.getOwnerId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}


