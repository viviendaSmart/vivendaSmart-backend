package com.firststudent.platform.viviendasmartbackend.property.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwnerIdAndDeletedFalse(Long ownerId);
    List<Property> findAllByDeletedFalse();
    Optional<Property> findByIdAndDeletedFalse(Long id);
}


