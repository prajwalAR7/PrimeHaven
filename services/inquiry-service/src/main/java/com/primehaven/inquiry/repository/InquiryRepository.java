package com.primehaven.inquiry.repository;

import com.primehaven.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByBuyerUsernameIgnoreCase(String buyerUsername);

    List<Inquiry> findByPropertyId(Long propertyId);

    List<Inquiry> findByBuyerUsernameIgnoreCaseAndPropertyId(String buyerUsername, Long propertyId);
}
