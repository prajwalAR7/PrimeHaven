package com.primehaven.inquiry.client.dto;

import java.time.LocalDate;

public class VisitCreateDto {

    private Long propertyId;
    private String buyerUsername;
    private LocalDate visitDate;
    private String timeSlot;
    private String notes;

    public VisitCreateDto() {
    }

    public VisitCreateDto(Long propertyId, String buyerUsername, LocalDate visitDate, String timeSlot, String notes) {
        this.propertyId = propertyId;
        this.buyerUsername = buyerUsername;
        this.visitDate = visitDate;
        this.timeSlot = timeSlot;
        this.notes = notes;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
