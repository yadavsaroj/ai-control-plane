package com.aicontrolplane.gateway.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChoiceDto(
        int        index,
        MessageDto message,
        @JsonProperty("finish_reason") String finishReason
) {}
