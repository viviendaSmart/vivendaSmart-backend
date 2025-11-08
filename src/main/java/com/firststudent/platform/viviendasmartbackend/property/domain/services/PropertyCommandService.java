package com.firststudent.platform.viviendasmartbackend.property.domain.services;

import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;

import java.math.BigDecimal;
import java.util.Optional;

public interface PropertyCommandService {
    Property create(String address, BigDecimal price, BigDecimal size, String photo, Long ownerId);
    Optional<Property> update(Long propertyId, String address, BigDecimal price, BigDecimal size, String photo);
    void delete(Long propertyId);
}


