package com.primehaven.property.dto;

import java.math.BigDecimal;

public class PropertyAvailabilityResponse {

    private Long propertyId;
    private boolean available;
    private String status;
    private String title;
    private BigDecimal price;

    public PropertyAvailabilityResponse() {
    }

    public PropertyAvailabilityResponse(Long propertyId, boolean available, String status, String title, BigDecimal price) {
        this.propertyId = propertyId;
        this.available = available;
        this.status = status;
        this.title = title;
        this.price = price;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
