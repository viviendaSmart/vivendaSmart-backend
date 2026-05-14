package com.firststudent.platform.viviendasmartbackend.ControllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.domain.services.PropertyCommandService;
import com.firststudent.platform.viviendasmartbackend.property.domain.services.PropertyQueryService;
import com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.PropertyController;
import com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.resources.CreatePropertyResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PropertyController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PropertyCommandService commandService;

    @MockitoBean
    private PropertyQueryService queryService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void create_ShouldReturnOk() throws Exception {
        // Arrange
        CreatePropertyResource request = new CreatePropertyResource(
                "Av. Principal 123",
                new BigDecimal("1500.00"),
                new BigDecimal("120.50"),
                "foto.jpg",
                1L
        );

        Property mockProperty = mock(Property.class);

        when(commandService.create(anyString(), any(BigDecimal.class), any(BigDecimal.class), anyString(), anyLong()))
                .thenReturn(mockProperty);

        // Act & Assert
        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        // Arrange
        Property mockProperty1 = mock(Property.class);
        Property mockProperty2 = mock(Property.class);
        List<Property> properties = Arrays.asList(mockProperty1, mockProperty2);

        when(queryService.getAll()).thenReturn(properties);

        // Act & Assert
        mockMvc.perform(get("/api/v1/properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getById_WhenExists_ShouldReturnOk() throws Exception {
        // Arrange
        Long propertyId = 1L;
        Property mockProperty = mock(Property.class);
        when(queryService.getById(propertyId)).thenReturn(Optional.of(mockProperty));

        // Act & Assert
        mockMvc.perform(get("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isOk());
    }

    @Test
    void getById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long propertyId = 99L;
        when(queryService.getById(propertyId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_WhenExists_ShouldReturnOk() throws Exception {
        // Arrange
        Long propertyId = 1L;
        CreatePropertyResource request = new CreatePropertyResource(
                "Nueva Dirección",
                new BigDecimal("2000.00"),
                new BigDecimal("150.00"),
                "foto2.jpg",
                1L
        );
        Property updatedProperty = mock(Property.class);

        when(commandService.update(anyLong(), anyString(), any(BigDecimal.class), any(BigDecimal.class), anyString()))
                .thenReturn(Optional.of(updatedProperty));

        // Act & Assert
        mockMvc.perform(put("/api/v1/properties/{id}", propertyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long propertyId = 1L;
        doNothing().when(commandService).delete(propertyId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isNoContent());
    }
}