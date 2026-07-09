package com.aicontrolplane.gateway.core;

import java.util.List;

public record ChatResponse(
        String       id,
        String       model,
        List<Choice> choices,
        Usage        usage
) {}
