package com.primehaven.inquiry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehaven.inquiry.client.PropertyClient;
import com.primehaven.inquiry.client.VisitClient;
import com.primehaven.inquiry.client.dto.PropertyAvailabilityDto;
import com.primehaven.inquiry.client.dto.VisitDto;
import com.primehaven.inquiry.dto.InquiryRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropertyClient propertyClient;

    @MockBean
    private VisitClient visitClient;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/inquiries/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("inquiry-service"));
    }

    @Test
    void testInquiryWithPropertyAndVisitOrchestration() throws Exception {
        Long propertyId = 5L;

        // 1. Mock Property Service availability check (Inquiry -> Property)
        PropertyAvailabilityDto availability = new PropertyAvailabilityDto(
                propertyId, true, "AVAILABLE", "Grand Beverly Villa", new BigDecimal("1500000.00")
        );
        Mockito.when(propertyClient.checkAvailability(eq(propertyId))).thenReturn(availability);

        // 2. Mock Visit Service schedule call (Inquiry -> Property -> Visit)
        VisitDto mockVisit = new VisitDto();
        mockVisit.setId(777L);
        mockVisit.setPropertyId(propertyId);
        mockVisit.setBuyerUsername("buyer_david");
        mockVisit.setVisitDate(LocalDate.now().plusDays(2));
        mockVisit.setTimeSlot("10:00 AM - 11:00 AM");
        mockVisit.setStatus("SCHEDULED");
        mockVisit.setCreatedAt(LocalDateTime.now());
        Mockito.when(visitClient.scheduleVisit(any())).thenReturn(mockVisit);

        // 3. Create composite Inquiry with visit request
        InquiryRequest request = new InquiryRequest();
        request.setPropertyId(propertyId);
        request.setBuyerUsername("buyer_david");
        request.setInquiryType("VISIT_REQUEST");
        request.setMessage("I would love to view this property in person.");
        request.setRequestVisit(true);
        request.setVisitDate(LocalDate.now().plusDays(2));
        request.setTimeSlot("10:00 AM - 11:00 AM");
        request.setVisitNotes("Please confirm with the host");

        mockMvc.perform(post("/api/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(5))
                .andExpect(jsonPath("$.propertyTitle").value("Grand Beverly Villa"))
                .andExpect(jsonPath("$.visitId").value(777))
                .andExpect(jsonPath("$.visitDetails.status").value("SCHEDULED"));
    }
}
