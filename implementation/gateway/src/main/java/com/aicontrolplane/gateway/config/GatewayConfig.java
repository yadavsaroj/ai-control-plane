package com.aicontrolplane.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {
    // Fields are added here as capabilities are introduced in subsequent milestones.
    //
    // Milestone 2: auth.apiKeys, routing.modelRoutes
    //   Each API key entry maps to an applicationId.
    //   The caller may also supply X-Application-Id explicitly (see GatewayHeaders).
    //   In v0.2, AuthFilter trusts the caller-supplied X-Application-Id header directly.
    //   In v0.5, applicationId is derived from the verified identity token instead.
    //
    // Milestone 3: providers.anthropic, providers.openai, metadata.queueCapacity
}
