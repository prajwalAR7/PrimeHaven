package com.primehaven.inquiry.controller;

import com.primehaven.inquiry.dto.InquiryRequest;
import com.primehaven.inquiry.dto.InquiryResponse;
import com.primehaven.inquiry.dto.InquiryStatusUpdateRequest;
import com.primehaven.inquiry.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "inquiry-service");
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }

    @PostMapping
    public ResponseEntity<InquiryResponse> createInquiry(@Valid @RequestBody InquiryRequest request) {
        InquiryResponse response = inquiryService.createInquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InquiryResponse>> getInquiries(
            @RequestParam(name = "buyerUsername", required = false) String buyerUsername,
            @RequestParam(name = "propertyId", required = false) Long propertyId) {
        List<InquiryResponse> inquiries = inquiryService.getInquiries(buyerUsername, propertyId);
        return ResponseEntity.ok(inquiries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getInquiryById(@PathVariable("id") Long id) {
        InquiryResponse inquiry = inquiryService.getInquiryById(id);
        return ResponseEntity.ok(inquiry);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InquiryResponse> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody InquiryStatusUpdateRequest request) {
        InquiryResponse updated = inquiryService.updateStatus(id, request);
        return ResponseEntity.ok(updated);
    }
}
