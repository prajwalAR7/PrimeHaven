package com.primehaven.visit.service;

import com.primehaven.visit.dto.VisitRequest;
import com.primehaven.visit.dto.VisitResponse;
import com.primehaven.visit.dto.VisitStatusUpdateRequest;
import com.primehaven.visit.entity.Visit;
import com.primehaven.visit.exception.ResourceNotFoundException;
import com.primehaven.visit.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VisitService {

    private final VisitRepository visitRepository;

    public VisitService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public VisitResponse scheduleVisit(VisitRequest request) {
        Visit visit = new Visit();
        visit.setPropertyId(request.getPropertyId());
        visit.setBuyerUsername(request.getBuyerUsername());
        visit.setVisitDate(request.getVisitDate());
        visit.setTimeSlot(request.getTimeSlot());
        visit.setNotes(request.getNotes());
        visit.setStatus("SCHEDULED");

        Visit saved = visitRepository.save(visit);
        return VisitResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> getVisits(String buyerUsername, Long propertyId) {
        List<Visit> visits;
        if (buyerUsername != null && propertyId != null) {
            visits = visitRepository.findByBuyerUsernameIgnoreCaseAndPropertyId(buyerUsername, propertyId);
        } else if (buyerUsername != null) {
            visits = visitRepository.findByBuyerUsernameIgnoreCase(buyerUsername);
        } else if (propertyId != null) {
            visits = visitRepository.findByPropertyId(propertyId);
        } else {
            visits = visitRepository.findAll();
        }

        return visits.stream()
                .map(VisitResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VisitResponse getVisitById(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + id));
        return VisitResponse.fromEntity(visit);
    }

    public VisitResponse updateStatus(Long id, VisitStatusUpdateRequest request) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + id));

        visit.setStatus(request.getStatus().toUpperCase());
        Visit updated = visitRepository.save(visit);
        return VisitResponse.fromEntity(updated);
    }

    public VisitResponse cancelVisit(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + id));

        visit.setStatus("CANCELLED");
        Visit updated = visitRepository.save(visit);
        return VisitResponse.fromEntity(updated);
    }
}
