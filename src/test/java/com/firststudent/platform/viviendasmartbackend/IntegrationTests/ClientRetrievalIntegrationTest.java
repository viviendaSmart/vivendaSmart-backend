package com.firststudent.platform.viviendasmartbackend.IntegrationTests;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.model.valueobjects.MaritalStatus;
import com.firststudent.platform.viviendasmartbackend.client.infrastructure.persistence.jpa.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class ClientRetrievalIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ClientRepository clientRepository;

    @Test
    void getClientById_WhenClientExists_ShouldReturnClientDetails() throws Exception {
        // 1. Arrange: Usamos el constructor público con todos los argumentos
        Client testClient = new Client(
                "12345678",                 // 1. dni
                new BigDecimal("4500.00"),  // 2. monthlyIncome
                "Arquitecto",               // 3. ocupation
                "Carlos",                   // 4. name
                "Gomez",                    // 5. surname
                "Los Olivos, Lima",         // 6. address
                "Constructora SAC",         // 7. business
                "Variable",                 // 8. earningtype
                true,                       // 9. credithistory
                true,                       // 10. support
                MaritalStatus.CASADO,       // 11. maritalStatus
                "999888777",                // 12. phoneNumber
                1L                          // 13. userId
        );

        Client savedClient = clientRepository.save(testClient);
        Long clientId = savedClient.getId();

        // 2. Act & 3. Assert: Hacemos la petición GET a la API
        mockMvc.perform(get("/api/v1/clients/" + clientId) // Ajusta la ruta si en tu controller es distinta
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos un 200 OK
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.name").value("Carlos"))
                .andExpect(jsonPath("$.monthlyIncome").value(4500.00));
    }
}