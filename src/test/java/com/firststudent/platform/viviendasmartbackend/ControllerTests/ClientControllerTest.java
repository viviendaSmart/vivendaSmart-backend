package com.firststudent.platform.viviendasmartbackend.ControllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientCommandService;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientQueryService;
import com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.ClientController;
import com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.resources.CreateClientResource;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ClientController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientCommandService commandService;

    @MockitoBean
    private ClientQueryService queryService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void create_ShouldReturnOk() throws Exception {
        // Arrange
        // TODO: Reemplaza los "null" con los Enums/ValueObjects reales de tu dominio
        CreateClientResource request = new CreateClientResource(
                "76543210",                     // dni
                new BigDecimal("3500.00"),      // monthlyIncome (asumiendo BigDecimal)
                null,                           // ocupation (Reemplaza con tu Enum)
                "Rafael",                       // name
                "Tasayco",                      // surname
                "Av. San Juan de Lurigancho",   // address
                null,                           // business
                null,                           // earningtype
                null,                           // credithistory
                null,                           // support
                null,                           // maritalStatus
                "987654321",                    // phoneNumber
                1L                              // userId
        );

        Client mockClient = mock(Client.class);

        when(commandService.create(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(mockClient);

        // Act & Assert
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        // Arrange
        Client mockClient1 = mock(Client.class);
        Client mockClient2 = mock(Client.class);
        List<Client> clients = Arrays.asList(mockClient1, mockClient2);

        when(queryService.getAll()).thenReturn(clients);

        // Act & Assert
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getById_WhenExists_ShouldReturnOk() throws Exception {
        // Arrange
        Long clientId = 1L;
        Client mockClient = mock(Client.class);

        when(queryService.getById(clientId)).thenReturn(Optional.of(mockClient));

        // Act & Assert
        mockMvc.perform(get("/api/v1/clients/{id}", clientId))
                .andExpect(status().isOk());
    }

    @Test
    void getByDni_WhenExists_ShouldReturnOk() throws Exception {
        // Arrange
        String dni = "76543210";
        Client mockClient = mock(Client.class);

        when(queryService.getByDni(dni)).thenReturn(Optional.of(mockClient));

        // Act & Assert
        mockMvc.perform(get("/api/v1/clients/by-dni")
                        .param("dni", dni))
                .andExpect(status().isOk());
    }

    @Test
    void getByUserId_ShouldReturnList() throws Exception {
        // Arrange
        Long userId = 1L;
        Client mockClient = mock(Client.class);
        List<Client> clients = List.of(mockClient);

        when(queryService.getByUserId(userId)).thenReturn(clients);

        // Act & Assert
        mockMvc.perform(get("/api/v1/clients/by-user-id")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void update_WhenExists_ShouldReturnOk() throws Exception {
        // Arrange
        Long clientId = 1L;

        CreateClientResource request = new CreateClientResource(
                "76543210",
                new BigDecimal("4000.00"),
                null, // ocupation
                "Rafael Augusto",
                "Tasayco",
                "Nueva dirección S.J.L.",
                null, // business
                null, // earningtype
                null, // credithistory
                null, // support
                null, // maritalStatus
                "987654321",
                1L
        );

        Client updatedClient = mock(Client.class);

        when(commandService.update(
                anyLong(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(Optional.of(updatedClient));

        // Act & Assert
        mockMvc.perform(put("/api/v1/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long clientId = 1L;
        doNothing().when(commandService).delete(clientId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/clients/{id}", clientId))
                .andExpect(status().isNoContent());
    }
}
