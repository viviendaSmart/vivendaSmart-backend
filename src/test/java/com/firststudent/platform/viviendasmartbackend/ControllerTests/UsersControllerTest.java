package com.firststudent.platform.viviendasmartbackend.ControllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.platform.viviendasmartbackend.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.firststudent.platform.viviendasmartbackend.iam.application.internal.queryservices.UserQueryServiceImpl;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.aggregates.User;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.queries.GetAllUsersQuery;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.queries.GetUserByEmailQuery;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.valueobjects.Roles;
import com.firststudent.platform.viviendasmartbackend.iam.domain.services.RoleValidationService;
import com.firststudent.platform.viviendasmartbackend.iam.interfaces.UsersController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UsersController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserCommandServiceImpl userCommandService;

    @MockitoBean
    private UserQueryServiceImpl userQueryService;

    @MockitoBean
    private RoleValidationService roleValidationService;

    // --- Mock solicitado para evitar errores de contexto de base de datos ---
    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void signUp_ShouldReturnCreated() throws Exception {
        // Arrange
        // El JSON ahora coincide exactamente con los campos de SignUpResource
        String signUpJsonPayload = """
                {
                    "email": "test@f1ntrack.com",
                    "password": "password123",
                    "firstName": "Juan",
                    "lastName": "Perez",
                    "requestedRole": "FINANCIAL_ADVISOR"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJsonPayload))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void signIn_ShouldReturnOkWithToken() throws Exception {
        // Arrange
        String email = "admin@f1ntrack.com";
        String signInJsonPayload = """
                {
                    "email": "admin@f1ntrack.com",
                    "password": "password123"
                }
                """;

        User mockUser = mock(User.class);
        // Preparamos el mock para que el Assembler no falle al mapearlo
        when(mockUser.getId()).thenReturn(1L);

        // Simulamos que el query service encuentra al usuario
        when(userQueryService.handle(any(GetUserByEmailQuery.class)))
                .thenReturn(Optional.of(mockUser));

        // Simulamos la generación del token
        String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fakeToken.Signature";
        when(userCommandService.generateTokenForUser(any(User.class)))
                .thenReturn(fakeToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signInJsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeToken))
                .andExpect(jsonPath("$.expiresIn").value(604800));
    }

    @Test
    void getUserByEmail_WhenExists_ShouldReturnOk() throws Exception {
        // Arrange
        String emailToSearch = "user@domain.com";
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(10L);

        when(userQueryService.handle(any(GetUserByEmailQuery.class)))
                .thenReturn(Optional.of(mockUser));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/by-email")
                        .param("email", emailToSearch)) // Pasamos el parámetro @RequestParam
                .andExpect(status().isOk());
    }

    @Test
    void getUserByEmail_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        String emailToSearch = "ghost@domain.com";

        when(userQueryService.handle(any(GetUserByEmailQuery.class)))
                .thenReturn(Optional.empty()); // Simulamos que no se encontró

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/by-email")
                        .param("email", emailToSearch))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        // Arrange
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        List<User> mockUsers = Arrays.asList(user1, user2);

        when(userQueryService.handle(any(GetAllUsersQuery.class))).thenReturn(mockUsers);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAvailableRoles_ShouldReturnOk() throws Exception {

        Roles[] mockRolesArray = new Roles[] { Roles.PROPERTY_MANAGER, Roles.FINANCIAL_ADVISOR };

        // Ahora los tipos coinciden perfectamente: un Roles[]
        when(roleValidationService.getAvailableRolesForRegistration()).thenReturn(mockRolesArray);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/available-roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("PROPERTY_MANAGER")); // Jackson serializa el Enum como String
    }
}
