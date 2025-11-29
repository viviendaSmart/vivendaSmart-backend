package com.firststudent.platform.viviendasmartbackend.simulator.domain.services;

import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates.Simulation;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;

import java.util.List;

public interface SimulationService {

    SimulationResult simulate(SimulationRequest request);

    List<Simulation> getAllSimulations();


    List<Simulation> getSimulationsByUserId(Long userId);

}
