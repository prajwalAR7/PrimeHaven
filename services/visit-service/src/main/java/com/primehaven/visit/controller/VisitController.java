package com.primehaven.visit.controller;

import com.primehaven.visit.dto.VisitRequest;
import com.primehaven.visit.dto.VisitResponse;
import com.primehaven.visit.dto.VisitStatusUpdateRequest;
import com.primehaven.visit.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "visit-service");
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }

    @PostMapping
    public ResponseEntity<VisitResponse> scheduleVisit(@Valid @RequestBody VisitRequest request) {
        VisitResponse scheduled = visitService.scheduleVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduled);
    }

    @GetMapping
    public ResponseEntity<List<VisitResponse>> getVisits(
            @RequestParam(name = "buyerUsername", required = false) String buyerUsername,
            @RequestParam(name = "propertyId", required = false) Long propertyId) {
        List<VisitResponse> visits = visitService.getVisits(buyerUsername, propertyId);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitResponse> getVisitById(@PathVariable("id") Long id) {
        VisitResponse visit = visitService.getVisitById(id);
        return ResponseEntity.ok(visit);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VisitResponse> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody VisitStatusUpdateRequest request) {
        VisitResponse updated = visitService.updateStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<VisitResponse> cancelVisit(@PathVariable("id") Long id) {
        VisitResponse cancelled = visitService.cancelVisit(id);
        return ResponseEntity.ok(cancelled);
    }
}
