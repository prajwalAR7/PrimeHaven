package com.primehaven.inquiry.client;

import com.primehaven.inquiry.client.dto.VisitCreateDto;
import com.primehaven.inquiry.client.dto.VisitDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "visit-service")
public interface VisitClient {

    @PostMapping("/api/visits")
    VisitDto scheduleVisit(@RequestBody VisitCreateDto request);
}
