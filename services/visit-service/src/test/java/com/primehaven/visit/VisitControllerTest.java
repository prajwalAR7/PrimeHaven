package com.primehaven.visit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehaven.visit.dto.VisitRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/visits/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("visit-service"));
    }

    @Test
    void testScheduleAndRetrieveVisit() throws Exception {
        VisitRequest request = new VisitRequest();
        request.setPropertyId(10L);
        request.setBuyerUsername("buyer_john");
        request.setVisitDate(LocalDate.now().plusDays(3));
        request.setTimeSlot("02:00 PM - 03:00 PM");
        request.setNotes("First inspection visit");

        // 1. Schedule Visit
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(10))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        // 2. Query Visit by buyerUsername
        mockMvc.perform(get("/api/visits?buyerUsername=buyer_john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyerUsername").value("buyer_john"));
    }
}
