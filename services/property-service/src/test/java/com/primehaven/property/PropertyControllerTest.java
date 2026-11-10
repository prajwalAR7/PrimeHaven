package com.primehaven.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehaven.property.dto.PropertyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/properties/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("property-service"));
    }

    @Test
    void testCreateAndGetProperty() throws Exception {
        PropertyRequest request = new PropertyRequest();
        request.setTitle("Sunset Ocean Villa");
        request.setDescription("Panoramic view with 3 bedrooms");
        request.setAddress("100 Ocean Drive");
        request.setCity("Malibu");
        request.setPrice(new BigDecimal("850000.00"));
        request.setPropertyType("VILLA");
        request.setStatus("AVAILABLE");
        request.setBedrooms(3);
        request.setBathrooms(2);
        request.setAgentUsername("agent_alice");

        // 1. Create property
        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Sunset Ocean Villa"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        // 2. Query property list
        mockMvc.perform(get("/api/properties?city=Malibu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Malibu"));
    }
}
