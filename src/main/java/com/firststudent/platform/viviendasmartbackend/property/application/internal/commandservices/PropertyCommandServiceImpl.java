package com.firststudent.platform.viviendasmartbackend.property.application.internal.commandservices;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.domain.services.PropertyCommandService;
import com.firststudent.platform.viviendasmartbackend.property.infrastructure.persistence.jpa.repositories.PropertyRepository;

@Service
public class PropertyCommandServiceImpl implements PropertyCommandService {

    private final PropertyRepository propertyRepository;

    public PropertyCommandServiceImpl(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional
    public Property create(String address, BigDecimal price, BigDecimal size, String photo, Long ownerId) {
        Property property = new Property(address, price, size, photo, ownerId);
        return propertyRepository.save(property);
    }

    @Override
    @Transactional
    public Optional<Property> update(Long propertyId, String address, BigDecimal price, BigDecimal size, String photo) {
        return propertyRepository.findById(propertyId).map(existing -> {
            existing.updateDetails(address, price, size, photo);
            return propertyRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(Long propertyId) {
        propertyRepository.deleteById(propertyId);
    }
}


