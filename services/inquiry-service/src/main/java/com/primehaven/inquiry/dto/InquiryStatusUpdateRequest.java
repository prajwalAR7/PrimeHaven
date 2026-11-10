package com.primehaven.inquiry.dto;

import jakarta.validation.constraints.NotBlank;

public class InquiryStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status; // NEW, IN_REVIEW, RESOLVED

    public InquiryStatusUpdateRequest() {
    }

    public InquiryStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
