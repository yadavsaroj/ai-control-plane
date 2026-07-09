package com.aicontrolplane.gateway.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    record HealthResponse(String status) {}

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok");
    }
}
