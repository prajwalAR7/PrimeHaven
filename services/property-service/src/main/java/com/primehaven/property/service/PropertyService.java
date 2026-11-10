package com.primehaven.property.service;

import com.primehaven.property.dto.PropertyAvailabilityResponse;
import com.primehaven.property.dto.PropertyRequest;
import com.primehaven.property.dto.PropertyResponse;
import com.primehaven.property.entity.Property;
import com.primehaven.property.exception.ResourceNotFoundException;
import com.primehaven.property.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public PropertyResponse createProperty(PropertyRequest request) {
        Property property = new Property();
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setPrice(request.getPrice());
        property.setPropertyType(request.getPropertyType().toUpperCase());
        property.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "AVAILABLE");
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setAgentUsername(request.getAgentUsername());

        Property saved = propertyRepository.save(property);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties(String city, String status, BigDecimal maxPrice) {
        List<Property> properties;
        if (city != null && status != null) {
            properties = propertyRepository.findByCityIgnoreCaseAndStatusIgnoreCase(city, status);
        } else if (city != null) {
            properties = propertyRepository.findByCityIgnoreCase(city);
        } else if (status != null) {
            properties = propertyRepository.findByStatusIgnoreCase(status);
        } else if (maxPrice != null) {
            properties = propertyRepository.findByPriceLessThanEqual(maxPrice);
        } else {
            properties = propertyRepository.findAll();
        }

        return properties.stream()
                .map(PropertyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + id));
        return PropertyResponse.fromEntity(property);
    }

    public PropertyResponse updateProperty(Long id, PropertyRequest request) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + id));

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setPrice(request.getPrice());
        property.setPropertyType(request.getPropertyType().toUpperCase());
        if (request.getStatus() != null) {
            property.setStatus(request.getStatus().toUpperCase());
        }
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        if (request.getAgentUsername() != null) {
            property.setAgentUsername(request.getAgentUsername());
        }

        Property updated = propertyRepository.save(property);
        return PropertyResponse.fromEntity(updated);
    }

    public void deleteProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + id));
        propertyRepository.delete(property);
    }

    @Transactional(readOnly = true)
    public PropertyAvailabilityResponse checkAvailability(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + id));

        boolean isAvailable = "AVAILABLE".equalsIgnoreCase(property.getStatus());
        return new PropertyAvailabilityResponse(
                property.getId(),
                isAvailable,
                property.getStatus(),
                property.getTitle(),
                property.getPrice()
        );
    }
}
