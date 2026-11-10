package com.primehaven.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class InquiryRequest {

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotBlank(message = "Buyer username is required")
    private String buyerUsername;

    @NotBlank(message = "Inquiry type is required")
    private String inquiryType; // PRICE_INQUIRY, VISIT_REQUEST, GENERAL

    @NotBlank(message = "Message cannot be blank")
    private String message;

    // Optional fields for automated visit scheduling (Inquiry -> Property -> Visit)
    private boolean requestVisit;
    private LocalDate visitDate;
    private String timeSlot;
    private String visitNotes;

    public InquiryRequest() {
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

    public String getInquiryType() {
        return inquiryType;
    }

    public void setInquiryType(String inquiryType) {
        this.inquiryType = inquiryType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRequestVisit() {
        return requestVisit;
    }

    public void setRequestVisit(boolean requestVisit) {
        this.requestVisit = requestVisit;
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

    public String getVisitNotes() {
        return visitNotes;
    }

    public void setVisitNotes(String visitNotes) {
        this.visitNotes = visitNotes;
    }
}
