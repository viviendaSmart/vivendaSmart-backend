package com.firststudent.platform.viviendasmartbackend.IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class PropertyUpdateIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void updateProperty_ShouldModifyExistingDataAndReturn200() throws Exception {
        String initialJson = "{\"address\":\"Casa Antigua\", \"price\":100000.0, \"size\":100.0, \"photo\":\"url\", \"ownerId\":1}";

        String responseContent = mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(initialJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseContent);
        Long generatedId = jsonNode.get("id").asLong();

        String updateJson = "{\"address\":\"Casa Remodelada\", \"price\":120000.0, \"size\":100.0, \"photo\":\"nueva-url\", \"ownerId\":1}";

        mockMvc.perform(put("/api/v1/properties/" + generatedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Casa Remodelada"))
                .andExpect(jsonPath("$.price").value(120000.0));
    }
}
