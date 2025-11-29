package com.firststudent.platform.viviendasmartbackend.simulator.infraestructure.persistence.jpa.repositories;

import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationRepository extends JpaRepository<Simulation, Long> {

    List<Simulation> findByUserId(Long userId);

    List<Simulation> findByClientId(Long clientId);

    List<Simulation> findByPropertyId(Long propertyId);
}