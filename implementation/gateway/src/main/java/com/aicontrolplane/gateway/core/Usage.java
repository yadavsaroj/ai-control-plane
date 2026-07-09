package com.aicontrolplane.gateway.core;

public record Usage(int promptTokens, int completionTokens, int totalTokens) {}
