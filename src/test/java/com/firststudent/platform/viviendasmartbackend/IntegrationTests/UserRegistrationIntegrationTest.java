package com.firststudent.platform.viviendasmartbackend.IntegrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.commands.SignUpCommand;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.valueobjects.Roles;
import com.firststudent.platform.viviendasmartbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class UserRegistrationIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_ShouldPersistEncryptedUser() throws Exception {
        var command = new SignUpCommand("test@f1ntrack.com", "Secret123", "Rafael", "Tasayco", Roles.FINANCIAL_ADVISOR);

        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail("test@f1ntrack.com");
        assertThat(user).isPresent();
        assertThat(user.get().getPasswordHash()).isNotEqualTo("Secret123");
    }
}