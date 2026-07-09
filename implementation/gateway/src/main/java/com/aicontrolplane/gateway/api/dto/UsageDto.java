package com.aicontrolplane.gateway.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UsageDto(
        @JsonProperty("prompt_tokens")     int promptTokens,
        @JsonProperty("completion_tokens") int completionTokens,
        @JsonProperty("total_tokens")      int totalTokens
) {}
