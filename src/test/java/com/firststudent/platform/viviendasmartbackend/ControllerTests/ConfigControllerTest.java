package com.firststudent.platform.viviendasmartbackend.ControllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.Exchange;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.GraceType;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.RateType;
import com.firststudent.platform.viviendasmartbackend.config.domain.services.ConfigCommandService;
import com.firststudent.platform.viviendasmartbackend.config.domain.services.ConfigQueryService;
import com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.ConfigController;
import com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.resources.CreateConfigResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ConfigController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConfigCommandService commandService;

    @MockitoBean
    private ConfigQueryService queryService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void create_ShouldReturnOk() throws Exception {
        // Arrange
        CreateConfigResource request = new CreateConfigResource(
                new BigDecimal("10.50"),
                RateType.TEA,      // <-- Pon aquí un valor real de tu Enum (ej. RateType.NOMINAL)
                Exchange.DOLARES,           // <-- Pon aquí un valor real de tu Enum o ValueObject
                GraceType.PARCIAL,      // <-- Pon aquí un valor real de tu Enum (ej. GraceType.TOTAL)
                12,
                1L
        );

        Config mockConfig = mock(Config.class);

        // 2. Usamos los matchers exactos de tus ValueObjects
        when(commandService.create(
                any(java.math.BigDecimal.class),
                any(RateType.class),
                any(Exchange.class),
                any(GraceType.class),
                any(Integer.class),
                any(Long.class)
        )).thenReturn(mockConfig);

        // Act & Assert
        mockMvc.perform(post("/api/v1/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    @Test
    void getById_WhenExists_ShouldReturnOk() throws Exception {
        // Arrange
        Long userId = 1L;
        Config mockConfig = mock(Config.class);

        // Atención: Tu endpoint mapea /{userId} y llama a getByUserId
        when(queryService.getByUserId(userId)).thenReturn(Optional.of(mockConfig));

        // Act & Assert
        mockMvc.perform(get("/api/v1/config/{userId}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void getById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long userId = 99L;
        when(queryService.getByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/config/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_WhenExists_ShouldReturnOk() throws Exception {
        // Arrange
        Long configId = 1L;

        // 1. Volvemos a instanciar el objeto de forma segura
        CreateConfigResource request = new CreateConfigResource(
                new BigDecimal("12.00"),
                RateType.TEB,      // <-- Pon aquí tu valor real
                Exchange.DOLARES,           // <-- Pon aquí tu valor real
                GraceType.PARCIAL,      // <-- Pon aquí tu valor real
                24,
                1L
        );

        Config updatedConfig = mock(Config.class);

        // 2. Fíjate que el update() de tu controlador recibe 6 parámetros (no recibe el userId)
        when(commandService.update(
                anyLong(),
                any(java.math.BigDecimal.class),
                any(RateType.class),
                any(Exchange.class),
                any(GraceType.class),
                any(Integer.class)
        )).thenReturn(Optional.of(updatedConfig));

        // Act & Assert
        mockMvc.perform(put("/api/v1/config/{id}", configId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Convierte a JSON seguro
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long configId = 1L;
        doNothing().when(commandService).delete(configId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/config/{id}", configId))
                .andExpect(status().isNoContent());
    }
}

