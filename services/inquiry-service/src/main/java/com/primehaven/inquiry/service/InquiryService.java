package com.primehaven.inquiry.service;

import com.primehaven.inquiry.client.PropertyClient;
import com.primehaven.inquiry.client.VisitClient;
import com.primehaven.inquiry.client.dto.PropertyAvailabilityDto;
import com.primehaven.inquiry.client.dto.VisitCreateDto;
import com.primehaven.inquiry.client.dto.VisitDto;
import com.primehaven.inquiry.dto.InquiryRequest;
import com.primehaven.inquiry.dto.InquiryResponse;
import com.primehaven.inquiry.dto.InquiryStatusUpdateRequest;
import com.primehaven.inquiry.entity.Inquiry;
import com.primehaven.inquiry.exception.BadRequestException;
import com.primehaven.inquiry.exception.ResourceNotFoundException;
import com.primehaven.inquiry.repository.InquiryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PropertyClient propertyClient;
    private final VisitClient visitClient;

    public InquiryService(InquiryRepository inquiryRepository,
                          PropertyClient propertyClient,
                          VisitClient visitClient) {
        this.inquiryRepository = inquiryRepository;
        this.propertyClient = propertyClient;
        this.visitClient = visitClient;
    }

    public InquiryResponse createInquiry(InquiryRequest request) {
        // Step 1: Verify property availability via Property Service (Inquiry -> Property)
        PropertyAvailabilityDto property;
        try {
            property = propertyClient.checkAvailability(request.getPropertyId());
        } catch (Exception ex) {
            throw new BadRequestException("Unable to verify property: Property ID " 
                    + request.getPropertyId() + " could not be resolved. Error: " + ex.getMessage());
        }

        if (property == null || !property.isAvailable()) {
            throw new BadRequestException("Property with ID " + request.getPropertyId() 
                    + " is not currently available for inquiries or visits.");
        }

        VisitDto scheduledVisit = null;
        Long visitId = null;

        // Step 2: If inspection requested, schedule visit via Visit Service (Inquiry -> Property -> Visit)
        if (request.isRequestVisit()) {
            if (request.getVisitDate() == null || request.getTimeSlot() == null || request.getTimeSlot().isBlank()) {
                throw new BadRequestException("visitDate and timeSlot must be provided when requestVisit is true.");
            }

            try {
                VisitCreateDto visitRequest = new VisitCreateDto(
                        request.getPropertyId(),
                        request.getBuyerUsername(),
                        request.getVisitDate(),
                        request.getTimeSlot(),
                        request.getVisitNotes() != null ? request.getVisitNotes() 
                                : ("Inspection visit for property: " + property.getTitle())
                );
                scheduledVisit = visitClient.scheduleVisit(visitRequest);
                if (scheduledVisit != null) {
                    visitId = scheduledVisit.getId();
                }
            } catch (Exception ex) {
                throw new BadRequestException("Unable to schedule property inspection visit: " + ex.getMessage());
            }
        }

        // Step 3: Persist inquiry in Inquiry database
        Inquiry inquiry = new Inquiry();
        inquiry.setPropertyId(request.getPropertyId());
        inquiry.setBuyerUsername(request.getBuyerUsername());
        inquiry.setInquiryType(request.getInquiryType().toUpperCase());
        inquiry.setMessage(request.getMessage());
        inquiry.setStatus("NEW");
        inquiry.setVisitId(visitId);

        Inquiry saved = inquiryRepository.save(inquiry);
        InquiryResponse response = InquiryResponse.fromEntity(saved);
        response.setPropertyTitle(property.getTitle());
        response.setVisitDetails(scheduledVisit);
        return response;
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> getInquiries(String buyerUsername, Long propertyId) {
        List<Inquiry> inquiries;
        if (buyerUsername != null && propertyId != null) {
            inquiries = inquiryRepository.findByBuyerUsernameIgnoreCaseAndPropertyId(buyerUsername, propertyId);
        } else if (buyerUsername != null) {
            inquiries = inquiryRepository.findByBuyerUsernameIgnoreCase(buyerUsername);
        } else if (propertyId != null) {
            inquiries = inquiryRepository.findByPropertyId(propertyId);
        } else {
            inquiries = inquiryRepository.findAll();
        }

        return inquiries.stream()
                .map(InquiryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InquiryResponse getInquiryById(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found with ID: " + id));
        return InquiryResponse.fromEntity(inquiry);
    }

    public InquiryResponse updateStatus(Long id, InquiryStatusUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found with ID: " + id));

        inquiry.setStatus(request.getStatus().toUpperCase());
        Inquiry updated = inquiryRepository.save(inquiry);
        return InquiryResponse.fromEntity(updated);
    }
}
