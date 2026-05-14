package com.firststudent.platform.viviendasmartbackend.ServiceTests;

import com.firststudent.platform.viviendasmartbackend.client.application.internal.commandservices.ClientCommandServiceImpl;
import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.model.valueobjects.MaritalStatus;
import com.firststudent.platform.viviendasmartbackend.client.infrastructure.persistence.jpa.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientCommandServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientCommandServiceImpl clientCommandService;

    @Test
    void create_ShouldSaveAndReturnClient() {
        // Arrange
        Client mockClient = mock(Client.class);
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // Act
        Client result = clientCommandService.create(
                "76543210",
                new BigDecimal("4500.00"),
                "Ingeniero",
                "Rafael",
                "Tasayco",
                "Av. San Juan de Lurigancho 123",
                "Independiente",
                "Variable",
                true,
                false,
                MaritalStatus.SOLTERO,
                "987654321",
                1L
        );

        // Assert
        assertNotNull(result);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void update_WhenClientExists_ShouldInvokeUpdateDetailsAndSave() {
        // Arrange
        Long clientId = 1L;
        Client existingClient = mock(Client.class);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(existingClient)).thenReturn(existingClient);

        // Act
        Optional<Client> result = clientCommandService.update(
                clientId,
                new BigDecimal("5000.00"),
                "Consultor",
                "Rafael Augusto",
                "Tasayco",
                "SJL",
                "Empresa SA",
                "Fijo",
                true,
                true,
                MaritalStatus.CASADO,
                "999888777"
        );

        // Assert
        assertTrue(result.isPresent());
        // Verificamos que se llamó a la lógica de actualización interna del agregado
        verify(existingClient).updateDetails(
                any(BigDecimal.class), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyBoolean(),
                anyBoolean(), any(MaritalStatus.class), anyString()
        );
        verify(clientRepository).save(existingClient);
    }

    @Test
    void update_WhenClientNotFound_ShouldReturnEmpty() {
        // Arrange
        Long clientId = 999L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act
        Optional<Client> result = clientCommandService.update(
                clientId, BigDecimal.ZERO, "", "", "", "", "", "", false, false, MaritalStatus.SOLTERO, ""
        );

        // Assert
        assertFalse(result.isPresent());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void delete_ShouldInvokeRepositoryDelete() {
        // Arrange
        Long clientId = 1L;
        doNothing().when(clientRepository).deleteById(clientId);

        // Act
        clientCommandService.delete(clientId);

        // Assert
        verify(clientRepository, times(1)).deleteById(clientId);
    }
}
