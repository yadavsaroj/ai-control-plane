package com.aicontrolplane.gateway.core;

import java.util.List;

public record ChatRequest(
        String        modelAlias,
        List<Message> messages,
        Integer       maxTokens,
        Double        temperature
) {}
