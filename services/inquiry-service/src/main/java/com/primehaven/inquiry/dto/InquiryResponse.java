package com.primehaven.inquiry.dto;

import com.primehaven.inquiry.client.dto.VisitDto;
import com.primehaven.inquiry.entity.Inquiry;
import java.time.LocalDateTime;

public class InquiryResponse {

    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private String buyerUsername;
    private String inquiryType;
    private String message;
    private String status;
    private Long visitId;
    private VisitDto visitDetails;
    private LocalDateTime createdAt;

    public InquiryResponse() {
    }

    public static InquiryResponse fromEntity(Inquiry inquiry) {
        InquiryResponse resp = new InquiryResponse();
        resp.setId(inquiry.getId());
        resp.setPropertyId(inquiry.getPropertyId());
        resp.setBuyerUsername(inquiry.getBuyerUsername());
        resp.setInquiryType(inquiry.getInquiryType());
        resp.setMessage(inquiry.getMessage());
        resp.setStatus(inquiry.getStatus());
        resp.setVisitId(inquiry.getVisitId());
        resp.setCreatedAt(inquiry.getCreatedAt());
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

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public VisitDto getVisitDetails() {
        return visitDetails;
    }

    public void setVisitDetails(VisitDto visitDetails) {
        this.visitDetails = visitDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
