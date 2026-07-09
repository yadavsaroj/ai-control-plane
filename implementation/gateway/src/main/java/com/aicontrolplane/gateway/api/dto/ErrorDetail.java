package com.aicontrolplane.gateway.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorDetail(
        String type,
        String message,
        @JsonProperty("request_id") String requestId
) {}
