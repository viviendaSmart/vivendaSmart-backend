package com.firststudent.platform.viviendasmartbackend.IntegrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class PropertyFilterByOwnerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void getProperties_ByOwnerId_ShouldReturnOnlyMatchingProperties() throws Exception {
        String propertyOwner1 = "{\"address\":\"SJL, Lima\", \"price\":150000.0, \"size\":75.5, \"photo\":\"url1\", \"ownerId\":1}";
        String propertyOwner2 = "{\"address\":\"Miraflores\", \"price\":250000.0, \"size\":90.0, \"photo\":\"url2\", \"ownerId\":2}";

        mockMvc.perform(post("/api/v1/properties").contentType(MediaType.APPLICATION_JSON).content(propertyOwner1));
        mockMvc.perform(post("/api/v1/properties").contentType(MediaType.APPLICATION_JSON).content(propertyOwner2));

        // 2. Act & Assert: Buscamos solo las del ownerId = 1
        mockMvc.perform(get("/api/v1/properties")
                        .param("ownerId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Solo debe traer 1 resultado
                .andExpect(jsonPath("$[0].address").value("SJL, Lima"));
    }
}