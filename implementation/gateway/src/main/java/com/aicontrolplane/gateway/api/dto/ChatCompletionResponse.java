package com.aicontrolplane.gateway.api.dto;

import java.util.List;

public record ChatCompletionResponse(
        String          id,
        String          object,
        long            created,
        String          model,
        List<ChoiceDto> choices,
        UsageDto        usage
) {}
