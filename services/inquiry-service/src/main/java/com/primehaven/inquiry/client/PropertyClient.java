package com.primehaven.inquiry.client;

import com.primehaven.inquiry.client.dto.PropertyAvailabilityDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "property-service")
public interface PropertyClient {

    @GetMapping("/api/properties/{id}/availability")
    PropertyAvailabilityDto checkAvailability(@PathVariable("id") Long id);
}
