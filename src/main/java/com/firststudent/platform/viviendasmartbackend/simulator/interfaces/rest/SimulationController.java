package com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest;

import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.resources.PropertyResource;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates.Simulation;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.SimulationService;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.transform.PropertyResourceFromEntityAssembler.toResourceFromEntity;

@RestController
@RequestMapping("/api/v1/simulator")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping
    public ResponseEntity<SimulationResult> simulate(@RequestBody SimulationRequest request) {
        SimulationResult result = simulationService.simulate(request);
        return ResponseEntity.ok(result);
    }
    @GetMapping
    public ResponseEntity<List<Simulation>> getAllSimulations(
            @RequestParam(name = "userId", required = false) Long userId
    ) {
        List<Simulation> simulations;
        if (userId != null) {
            simulations = simulationService.getSimulationsByUserId(userId);
        } else {
            simulations = simulationService.getAllSimulations();
        }
        return ResponseEntity.ok(simulations);
    }


}
