package com.primehaven.property.dto;

import com.primehaven.property.entity.Property;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PropertyResponse {

    private Long id;
    private String title;
    private String description;
    private String address;
    private String city;
    private BigDecimal price;
    private String propertyType;
    private String status;
    private Integer bedrooms;
    private Integer bathrooms;
    private String agentUsername;
    private LocalDateTime createdAt;

    public PropertyResponse() {
    }

    public static PropertyResponse fromEntity(Property property) {
        PropertyResponse resp = new PropertyResponse();
        resp.setId(property.getId());
        resp.setTitle(property.getTitle());
        resp.setDescription(property.getDescription());
        resp.setAddress(property.getAddress());
        resp.setCity(property.getCity());
        resp.setPrice(property.getPrice());
        resp.setPropertyType(property.getPropertyType());
        resp.setStatus(property.getStatus());
        resp.setBedrooms(property.getBedrooms());
        resp.setBathrooms(property.getBathrooms());
        resp.setAgentUsername(property.getAgentUsername());
        resp.setCreatedAt(property.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
