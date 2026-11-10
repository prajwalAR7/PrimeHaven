package com.primehaven.property.controller;

import com.primehaven.property.dto.PropertyAvailabilityResponse;
import com.primehaven.property.dto.PropertyRequest;
import com.primehaven.property.dto.PropertyResponse;
import com.primehaven.property.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "property-service");
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(@Valid @RequestBody PropertyRequest request) {
        PropertyResponse created = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice) {
        List<PropertyResponse> properties = propertyService.getAllProperties(city, status, maxPrice);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable("id") Long id) {
        PropertyResponse property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable("id") Long id,
            @Valid @RequestBody PropertyRequest request) {
        PropertyResponse updated = propertyService.updateProperty(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProperty(@PathVariable("id") Long id) {
        propertyService.deleteProperty(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Property deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<PropertyAvailabilityResponse> checkAvailability(@PathVariable("id") Long id) {
        PropertyAvailabilityResponse availability = propertyService.checkAvailability(id);
        return ResponseEntity.ok(availability);
    }
}
