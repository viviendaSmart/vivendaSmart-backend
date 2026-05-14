package com.firststudent.platform.viviendasmartbackend.ServiceTests;

import com.firststudent.platform.viviendasmartbackend.property.application.internal.commandservices.PropertyCommandServiceImpl;
import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.infrastructure.persistence.jpa.repositories.PropertyRepository;
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
class PropertyCommandServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyCommandServiceImpl propertyCommandService;

    @Test
    void create_ShouldReturnSavedProperty() {
        // Arrange
        Property mockProperty = mock(Property.class);
        when(propertyRepository.save(any(Property.class))).thenReturn(mockProperty);

        // Act
        Property result = propertyCommandService.create(
                "Av. Las Flores 456, SJL",
                new BigDecimal("250000.00"),
                new BigDecimal("95.50"),
                "departamento.jpg",
                1L
        );

        // Assert
        assertNotNull(result);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    void update_WhenPropertyExists_ShouldReturnUpdatedProperty() {
        // Arrange
        Long propertyId = 1L;
        Property existingProperty = mock(Property.class);

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(existingProperty));
        when(propertyRepository.save(existingProperty)).thenReturn(existingProperty);

        // Act
        Optional<Property> result = propertyCommandService.update(
                propertyId,
                "Nueva Direccion 789",
                new BigDecimal("260000.00"),
                new BigDecimal("95.50"),
                "nueva_foto.jpg"
        );

        // Assert
        assertTrue(result.isPresent());
        // Verificamos que se llamó al método de actualización dentro de la entidad
        verify(existingProperty).updateDetails(anyString(), any(BigDecimal.class), any(BigDecimal.class), anyString());
        // Verificamos que se guardaron los cambios en el repositorio
        verify(propertyRepository).save(existingProperty);
    }

    @Test
    void update_WhenPropertyDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        Long propertyId = 99L;
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // Act
        Optional<Property> result = propertyCommandService.update(
                propertyId, "Direccion", BigDecimal.ZERO, BigDecimal.ZERO, "foto.jpg"
        );

        // Assert
        assertFalse(result.isPresent());
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        // Arrange
        Long propertyId = 1L;
        doNothing().when(propertyRepository).deleteById(propertyId);

        // Act
        propertyCommandService.delete(propertyId);

        // Assert
        verify(propertyRepository, times(1)).deleteById(propertyId);
    }
}
