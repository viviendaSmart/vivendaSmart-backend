package com.firststudent.platform.viviendasmartbackend.ServiceTests;
import com.firststudent.platform.viviendasmartbackend.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.firststudent.platform.viviendasmartbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.firststudent.platform.viviendasmartbackend.iam.application.internal.outboundservices.tokens.TokenService;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.aggregates.User;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.commands.SignUpCommand;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.entities.Role;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.exceptions.UserAlreadyExistsException;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.valueobjects.Roles;
import com.firststudent.platform.viviendasmartbackend.iam.domain.services.RoleValidationService;
import com.firststudent.platform.viviendasmartbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.firststudent.platform.viviendasmartbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private HashingService hashingService;
    @Mock
    private TokenService tokenService;
    @Mock
    private RoleValidationService roleValidationService;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    @Test
    void handleSignUp_SuccessfulRegistration() {
        // Arrange
        SignUpCommand command = new SignUpCommand(
                "nuevo@f1ntrack.com", "password123", "Rafael", "Tasayco", Roles.FINANCIAL_ADVISOR
        );

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleValidationService.canRequestRole(any())).thenReturn(true);
        when(hashingService.encode(anyString())).thenReturn("hashed_password");
        Role mockRole = new Role(Roles.FINANCIAL_ADVISOR);
        when(roleRepository.findByName(Roles.FINANCIAL_ADVISOR)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenReturn(mock(User.class));
        // Act
        assertDoesNotThrow(() -> userCommandService.handle(command));

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(hashingService, times(1)).encode("password123");
    }

    @Test
    void handleSignUp_ThrowsUserAlreadyExistsException() {
        // Arrange
        SignUpCommand command = new SignUpCommand(
                "existe@f1ntrack.com", "pass", "Juan", "Perez", Roles.FINANCIAL_ADVISOR
        );

        // Simulamos que el repositorio ya tiene ese email
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userCommandService.handle(command));

        // Verificamos que NUNCA se intentó guardar nada después del error
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void generateTokenForUser_ShouldReturnToken() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        // Simulamos que no tiene roles para que tome el default de tu código
        when(mockUser.getRoles()).thenReturn(java.util.Collections.emptyList());

        String expectedToken = "fake-jwt-token";
        when(tokenService.generateToken(anyLong(), anyString())).thenReturn(expectedToken);

        // Act
        String token = userCommandService.generateTokenForUser(mockUser);

        // Assert
        assertEquals(expectedToken, token);
        verify(tokenService).generateToken(1L, "CAR_OWNER");
    }
}
