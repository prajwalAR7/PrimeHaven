package com.primehaven.property.repository;

import com.primehaven.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByCityIgnoreCase(String city);

    List<Property> findByStatusIgnoreCase(String status);

    List<Property> findByCityIgnoreCaseAndStatusIgnoreCase(String city, String status);

    List<Property> findByPriceLessThanEqual(BigDecimal maxPrice);
}
