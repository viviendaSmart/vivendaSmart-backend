package com.firststudent.platform.viviendasmartbackend.property.domain.services;

import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;

import java.util.List;
import java.util.Optional;

public interface PropertyQueryService {
    List<Property> getAll();
    Optional<Property> getById(Long id);
    List<Property> getByOwnerId(Long ownerId);
}


