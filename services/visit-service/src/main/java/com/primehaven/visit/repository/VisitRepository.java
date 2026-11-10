package com.primehaven.visit.repository;

import com.primehaven.visit.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByBuyerUsernameIgnoreCase(String buyerUsername);

    List<Visit> findByPropertyId(Long propertyId);

    List<Visit> findByBuyerUsernameIgnoreCaseAndPropertyId(String buyerUsername, Long propertyId);
}
