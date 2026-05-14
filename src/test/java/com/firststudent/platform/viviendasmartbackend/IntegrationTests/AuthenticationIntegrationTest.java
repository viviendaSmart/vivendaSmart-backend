package com.firststudent.platform.viviendasmartbackend.IntegrationTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.commands.SignInCommand;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.commands.SignUpCommand;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void signIn_WithValidCredentials_ShouldReturnToken() throws Exception {
        var signUp = new SignUpCommand("rafael@f1ntrack.com", "Password123", "Rafael", "Tasayco", Roles.FINANCIAL_ADVISOR);
        mockMvc.perform(post("/api/v1/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUp)));

        var signIn = new SignInCommand("rafael@f1ntrack.com", "Password123");

        mockMvc.perform(post("/api/v1/users/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signIn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
