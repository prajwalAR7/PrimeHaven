package com.primehaven.visit.dto;

import jakarta.validation.constraints.NotBlank;

public class VisitStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status; // SCHEDULED, CONFIRMED, COMPLETED, CANCELLED

    public VisitStatusUpdateRequest() {
    }

    public VisitStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
