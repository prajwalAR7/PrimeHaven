package com.primehaven.visit.dto;

import com.primehaven.visit.entity.Visit;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VisitResponse {

    private Long id;
    private Long propertyId;
    private String buyerUsername;
    private LocalDate visitDate;
    private String timeSlot;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    public VisitResponse() {
    }

    public static VisitResponse fromEntity(Visit visit) {
        VisitResponse resp = new VisitResponse();
        resp.setId(visit.getId());
        resp.setPropertyId(visit.getPropertyId());
        resp.setBuyerUsername(visit.getBuyerUsername());
        resp.setVisitDate(visit.getVisitDate());
        resp.setTimeSlot(visit.getTimeSlot());
        resp.setStatus(visit.getStatus());
        resp.setNotes(visit.getNotes());
        resp.setCreatedAt(visit.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public void setBuyerUsername(String buyerUsername) {
        this.buyerUsername = buyerUsername;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
