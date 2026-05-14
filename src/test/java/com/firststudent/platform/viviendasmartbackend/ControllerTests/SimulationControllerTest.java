package com.firststudent.platform.viviendasmartbackend.ControllerTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates.Simulation;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.services.SimulationService;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.SimulationController;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationRequest;
import com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources.SimulationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = SimulationController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulationService simulationService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void simulate_ShouldReturnOk() throws Exception {
        // Arrange
        SimulationRequest request = new SimulationRequest();
        SimulationResult result = new SimulationResult();

        when(simulationService.simulate(any(SimulationRequest.class))).thenReturn(result);

        // Act & Assert
        mockMvc.perform(post("/api/v1/simulator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAllSimulations_ShouldReturnListOfSimulations() throws Exception {
        // Arrange
        List<Simulation> simulations = Arrays.asList(new Simulation(), new Simulation());
        when(simulationService.getAllSimulations()).thenReturn(simulations);

        // Act & Assert
        mockMvc.perform(get("/api/v1/simulator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllSimulations_WithUserId_ShouldReturnFilteredSimulations() throws Exception {
        // Arrange
        Long userId = 1L;
        List<Simulation> simulations = Arrays.asList(new Simulation());
        when(simulationService.getSimulationsByUserId(userId)).thenReturn(simulations);

        // Act & Assert
        mockMvc.perform(get("/api/v1/simulator")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
