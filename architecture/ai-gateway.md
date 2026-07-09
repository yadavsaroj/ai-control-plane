# AI Gateway

**Status:** Accepted
**Version:** 0.2
**Last Updated:** July 2026

---

# Purpose

The AI Gateway is the first core capability of the AI Control Plane.

It provides a common entry point for AI applications to access foundation models and AI infrastructure without coupling each application directly to individual providers, model APIs, authentication mechanisms, retry behavior, or operational controls.

The gateway is intentionally the first component in the reference platform because every future AI Control Plane capability — routing, policy, observability, evaluation, reliability, cost management — depends on request flow visibility and centralized access control.

---

# Problem Statement

In early AI adoption, application teams often integrate directly with model providers.

This works when there are only a few applications.

As adoption grows, direct integration creates operational problems:

* Each application manages provider credentials independently.
* Retry, timeout, and fallback behavior becomes inconsistent.
* Cost tracking is fragmented.
* Request logging is incomplete.
* Model selection is duplicated across teams.
* Policy enforcement is difficult to centralize.
* Provider-specific APIs leak into application code.
* Observability is inconsistent across AI workloads.

The result is a fragmented AI operating model.

The AI Gateway addresses this by centralizing the request path between AI applications and model providers.

---

# Goals

The AI Gateway should:

* Provide a single entry point for AI requests.
* Abstract model provider differences.
* Support multiple providers over time.
* Enable request routing.
* Provide consistent authentication and authorization hooks.
* Capture request metadata for observability and cost attribution.
* Standardize retries, timeouts, and error handling.
* Provide extension points for policy, evaluation, and reliability.
* Avoid tightly coupling applications to any single provider.

---

# Non-Goals

The AI Gateway does not attempt to:

* Build agents.
* Manage agent memory.
* Perform workflow orchestration.
* Replace agent frameworks.
* Train or fine-tune models.
* Define business-specific prompts.
* Own application-specific evaluation logic.
* Provide a complete observability platform in v0.2.

Those concerns belong to other layers of the AI stack or future AI Control Plane capabilities.

---

# Resolved Design Decisions

The following questions were open in v0.1 and are now resolved.

## API Surface: OpenAI-Compatible

The gateway exposes an OpenAI-compatible API.

This is not a commitment to OpenAI as a provider. It is a pragmatic choice: the schema is battle-tested, all major providers can be normalized to it, and any application already built on an OpenAI client can adopt the gateway by changing a single base URL.

A native AI Control Plane API can be layered on top in a future version once the gateway abstraction is validated.

## API Mode: Synchronous First, Streaming Deferred

The v0.2 API is synchronous. The application sends a request and waits for a complete response.

Streaming (Server-Sent Events) is deferred to v0.8. Middleware, error handling, metadata capture, and cost tracking all assume a complete request/response lifecycle. Streaming breaks those assumptions and requires a redesign of the response pipeline. The synchronous path must be stable before streaming is introduced.

An asynchronous job-based API (`POST /v1/jobs`, `GET /v1/jobs/{id}`) is a future capability for long-running batch and agent workloads.

## Request Metadata: Synchronous Capture, Asynchronous Emit

Metadata is captured in-process on every request. It is emitted asynchronously to avoid blocking the request path. In v0.2, emission is structured JSON to stdout. Future versions will emit to an event stream or queryable store without changing the capture interface.

## Routing: Before Policy Evaluation

Routing selects the provider and model. Policy evaluation determines whether a request is allowed. Routing happens first. Policy is introduced in v0.5 and operates on an already-resolved route.

## Model Names: Alias Registry

Applications reference models by alias (`claude-latest`, `gpt-4o`). The gateway resolves aliases to specific versioned provider models via a routing table. Aliases are updated centrally without requiring application changes.

## Implementation Language: Java 21

The reference implementation uses Java 21 with Spring Boot 3.x. Virtual threads (Project Loom) are used for concurrency, making the blocking request model performant for high-concurrency I/O-bound gateway workloads without the complexity of reactive programming.

---

# Reference Architecture

```text
AI Applications
Agents • Copilots • Internal Tools • APIs
        │
        ▼
+-----------------------------+
|         AI Gateway          |
+-----------------------------+
| Request API (OpenAI-compat) |
| Provider Abstraction        |
| Routing (Alias Registry)    |
| Authentication Hooks        |
| Retry / Timeout Handling    |
| Request Metadata Capture    |
| Error Normalization         |
+-----------------------------+
        │
        ▼
Provider Adapters
Anthropic • OpenAI • Gemini • Self-hosted Models
        │
        ▼
Foundation Models & AI Infrastructure
```

---

# Technology Stack

| Concern | Choice | Rationale |
|---|---|---|
| Language | Java 21 | Virtual threads make the blocking model performant for I/O-bound proxy workloads |
| Framework | Spring Boot 3.x | Dependency injection makes provider and router interfaces trivially swappable; well-understood in enterprise teams |
| HTTP server | Spring Web (blocking + virtual threads) | Simpler to reason about than reactive; comparable performance for this workload |
| HTTP client | Spring RestClient | Synchronous, composable, full control over timeouts and retries |
| Configuration | `application.yaml` via Spring config | Built-in env-var override; no additional library needed |
| Structured logging | SLF4J + Logback + Logstash JSON encoder | Structured JSON output; standard in Java ecosystem |
| Testing | JUnit 5 + Mockito + Spring Boot Test + WireMock | Full coverage from unit to in-process integration tests |
| Build | Maven or Gradle | Either works; choose whichever the team already operates |
| Container | Docker multi-stage (eclipse-temurin:21-jre) | Minimal production image; fast CI builds |

---

# Project Structure

```
implementation/gateway/
├── src/
│   ├── main/
│   │   ├── java/com/aicontrolplane/gateway/
│   │   │   ├── GatewayApplication.java
│   │   │   ├── api/
│   │   │   │   ├── ChatController.java
│   │   │   │   ├── ModelsController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── middleware/
│   │   │   │   ├── RequestIdFilter.java
│   │   │   │   ├── AuthFilter.java
│   │   │   │   └── LoggingFilter.java
│   │   │   ├── provider/
│   │   │   │   ├── Provider.java
│   │   │   │   ├── mock/MockProvider.java
│   │   │   │   ├── anthropic/AnthropicProvider.java
│   │   │   │   └── openai/OpenAiProvider.java
│   │   │   ├── router/
│   │   │   │   ├── Router.java
│   │   │   │   └── StaticRouter.java
│   │   │   ├── metadata/
│   │   │   │   └── MetadataRecorder.java
│   │   │   ├── errors/
│   │   │   │   └── GatewayException.java
│   │   │   └── config/
│   │   │       └── GatewayConfig.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/com/aicontrolplane/gateway/
├── pom.xml
├── Dockerfile
└── README.md
```

The `provider`, `router`, and `metadata` packages each contain one interface and one or more implementations. New providers, routers, and recorders are added without modifying existing code.

---

# Core Responsibilities

## 1. Request Entry Point

The gateway exposes a stable, OpenAI-compatible API that AI applications call.

For v0.2, the API surface is intentionally minimal:

```text
POST /v1/chat/completions    — submit a completion request
GET  /v1/models              — list available model aliases
GET  /health                 — liveness check
```

The goal is to validate the gateway abstraction before expanding into streaming, batch, and agent workflows.

---

## 2. Provider Abstraction

The gateway hides provider-specific implementation details from applications.

Application teams should not need to know:

* Which provider is serving the request.
* How provider credentials are stored.
* How provider-specific error formats work.
* How retry behavior differs across providers.
* How model names map to backend providers.

The gateway translates application requests into provider-specific calls through adapters. Each adapter is a self-contained implementation of the `Provider` interface.

---

## 3. Routing

Routing determines which provider and model should serve a request.

For v0.2, routing is static and configuration-driven. Applications reference model aliases. The static router resolves aliases to a provider and versioned model name via a YAML routing table.

Future routing strategies may include:

* Fallback routing on provider failure
* Cost-aware routing
* Latency-aware routing
* Weighted routing for model experimentation
* Tenant-specific routing
* Policy-driven routing

The v0.2 implementation defines routing as an explicit extension point. The `Router` interface is the boundary. Future routers implement the same interface without changing the data plane.

---

## 4. Authentication and Authorization Hooks

The gateway provides a place for identity and authorization checks.

For v0.2, authentication is a simple API key validated in a servlet filter. The resolved application ID is attached to the request context and flows through to metadata capture.

Future versions may support:

* Application identity with per-application permissions
* User identity propagation
* Tenant isolation
* Role-based access control
* Model access policies
* Audit logging

The design decision is that authentication and authorization belong in the gateway path, not inside every AI application.

---

## 5. Request Metadata Capture

Every request through the gateway captures the following metadata:

* Request ID
* Application ID
* Provider
* Model
* Latency (ms)
* HTTP status
* Normalized error category (if applicable)
* Input token count (when available)
* Output token count (when available)
* Timestamp

Metadata is captured synchronously in-process. Emission is asynchronous to avoid blocking the request path. In v0.2, emission is structured JSON to stdout via a background thread. The `MetadataRecorder` interface is the boundary — future versions emit to an event stream or queryable store without changing call sites.

This metadata is the foundation for future observability, cost management, reliability, and incident response capabilities.

---

## 6. Error Normalization

Different model providers expose different error formats, status codes, and error messages.

The gateway normalizes all provider errors into a consistent format before returning them to applications.

Normalized error categories:

| Category | HTTP Status | Meaning |
|---|---|---|
| `authentication_error` | 401 | Gateway API key invalid or missing |
| `provider_auth_error` | 502 | Gateway's provider credential was rejected |
| `rate_limit_error` | 429 | Provider rate limit exceeded |
| `timeout` | 504 | Provider did not respond within the deadline |
| `provider_unavailable` | 502 | Provider returned a 5xx error |
| `invalid_request` | 400 | Malformed request body |
| `unknown_error` | 500 | Unclassified error |

All error responses include: `error.type`, `error.message`, `error.request_id`.

Applications handle failures consistently regardless of provider.

---

## 7. Reliability Hooks

The gateway provides a natural place for reliability controls.

For v0.2:

* Per-request timeout enforced on provider calls
* Basic retry on `rate_limit_error` and `provider_unavailable` (max 2 retries, exponential backoff)
* `GET /health` liveness endpoint
* Provider errors logged with full context for debugging

Future versions may add:

* Circuit breakers per provider
* Fallback routing on provider failure
* Degraded mode responses
* SLO tracking
* Provider health checks running on a background schedule

The v0.2 reliability interfaces are designed to accommodate these without structural changes.

---

# Core Interfaces

These three interfaces are the architectural skeleton of the gateway. Everything else is an implementation.

## Provider

```
Provider
  getName(): String
  chat(context, ChatRequest): ChatResponse
  listModels(context): List<Model>
```

Provider-specific HTTP clients, credential handling, request translation, response mapping, and error mapping all live inside the adapter. Nothing leaks outward. New providers implement this interface and are registered via Spring dependency injection.

## Router

```
Router
  route(context, ChatRequest): RouteResult
```

`RouteResult` carries the resolved provider name and backend model name. The static router reads from a YAML routing table. Future routers — weighted, cost-aware, policy-driven — implement the same interface.

## MetadataRecorder

```
MetadataRecorder
  record(RequestRecord): void
```

`RequestRecord` carries all metadata fields listed in section 5. The call is non-blocking. The v0.2 implementation writes to a bounded async queue consumed by a background thread emitting JSON to stdout.

---

# Request Pipeline

The internal request pipeline is a standard servlet filter chain. Each filter handles one concern and delegates to the next.

```text
Incoming Request
    │
    ▼
[RequestIdFilter]     — generate X-Request-ID if absent; attach to MDC
    │
    ▼
[AuthFilter]          — validate API key; attach application ID to context
    │
    ▼
[LoggingFilter]       — log request start with request ID, method, path
    │
    ▼
ChatController
    ├── Router.route()              — resolve provider and model from alias
    ├── Provider.chat()             — call provider adapter
    ├── error normalization         — map provider exception to GatewayException
    ├── MetadataRecorder.record()   — enqueue metadata record (non-blocking)
    └── return normalized response
    │
    ▼
[LoggingFilter]       — log response status and latency
    │
    ▼
Response
```

---

# Configuration

```yaml
server:
  port: 8080
  read-timeout: 30s
  write-timeout: 60s

auth:
  api-keys:
    - key: "${GATEWAY_API_KEY}"
      application-id: "default"

routing:
  default-provider: anthropic
  model-routes:
    - alias: "claude-latest"
      provider: anthropic
      model: "claude-sonnet-4-6"
    - alias: "gpt-4o"
      provider: openai
      model: "gpt-4o"
    - alias: "mock"
      provider: mock
      model: "mock-model"

providers:
  anthropic:
    api-key-env: "ANTHROPIC_API_KEY"
    timeout: 30s
    max-retries: 2
  openai:
    api-key-env: "OPENAI_API_KEY"
    timeout: 30s
    max-retries: 2
```

Provider credentials are never stored in YAML. They resolve from environment variables at startup. The application fails fast at startup if a required credential is missing.

---

# API Modes

## v0.2 — Synchronous

The application sends a request and waits for the complete response. Simple, debuggable, works with all HTTP clients. LLM provider latency (200ms–30s) dominates — gateway overhead is negligible.

## v0.8 — Streaming (Server-Sent Events)

The connection stays open and tokens arrive as they are generated. Applications render output progressively. This requires a redesign of the response pipeline, middleware assumptions, metadata capture, and cost tracking. It is deferred until the synchronous path is stable.

## Future — Asynchronous Job API

For long-running batch and agent workloads that exceed synchronous timeout limits:

```text
POST /v1/jobs              → {"job_id": "abc123"}
GET  /v1/jobs/{id}         → {"status": "running"}
GET  /v1/jobs/{id}         → {"status": "complete", "result": {...}}
```

This decouples application request handling from provider response time entirely.

---

# Request Flow

```text
Application Request
        │
        ▼
Gateway API (OpenAI-compatible)
        │
        ▼
Validate API Key → attach Application ID
        │
        ▼
Resolve Route (alias → provider + model)
        │
        ▼
Invoke Provider Adapter
        │
        ▼
Normalize Response or Error
        │
        ▼
Record Request Metadata (async)
        │
        ▼
Return Response to Application
```

---

# Scalability Considerations

The gateway introduces an additional network hop. The following bottlenecks are known and should be accounted for in the design.

## Provider Rate Limits

This is the first bottleneck in practice. Centralizing all AI traffic behind one gateway aggregates demand against provider rate limits. Ten applications each making modest requests can collectively saturate a limit that none would hit individually.

Mitigations: per-application rate limits enforced at the gateway before requests reach the provider; token-aware throttling in addition to request-count throttling; multiple API keys per provider in future versions.

## Concurrent Connections

LLM requests hold open HTTP connections for seconds to tens of seconds. The gateway must manage high numbers of concurrent inbound and outbound connections simultaneously. Java 21 virtual threads handle this without the thread-per-connection overhead of traditional blocking servers.

## Metadata Recording

Synchronous metadata writes on the hot path become a bottleneck under load. The `MetadataRecorder` is always called asynchronously via a bounded queue. Under extreme load, records may be dropped rather than degrading request latency. This is an explicit design trade-off.

## Routing Configuration Lookups

Static YAML routing in v0.2 is an in-memory lookup with no scalability concern. When routing becomes dynamic (v0.4), configuration must be cached in-memory with a short TTL. The data plane must never block on a configuration store lookup during request processing.

## Stateless Design

The gateway must be stateless from the first version. No in-memory state that cannot be reconstructed from configuration. This makes horizontal scaling behind a load balancer trivial when traffic grows.

## The Primary Bottleneck

For an LLM gateway, provider latency and provider rate limits dominate operational concerns. Gateway throughput is almost never the constraint. Design for statelessness and horizontal scalability, then focus operational energy on managing provider capacity.

---

# Platform Boundaries

## Owned by the AI Gateway

* Stable, provider-neutral model access API
* Provider abstraction and adapters
* Model alias registry and routing
* Authentication hooks
* Retry and timeout behavior
* Error normalization
* Request metadata capture

## Not Owned by the AI Gateway

* Business logic
* Prompt design
* Agent planning and orchestration
* Tool execution logic
* Domain-specific evaluation
* User experience
* Product workflows

This boundary keeps the gateway focused on platform concerns.

---

# Alternatives Considered

## Direct Provider Integration

Applications call model providers directly.

### Advantages

* Simple for early prototypes
* No additional platform layer
* Full provider-specific control

### Disadvantages

* Duplicated provider integration logic
* Inconsistent observability
* Difficult governance
* Fragmented cost tracking
* Hard to change providers
* Operational behavior varies by application

---

## Agent Framework-Owned Gateway

Each agent framework provides its own gateway or provider abstraction.

### Advantages

* Convenient for applications using that framework
* Tight integration with agent execution

### Disadvantages

* Framework-specific operational behavior
* Hard to enforce enterprise-wide policies
* Difficult cross-framework observability
* Does not solve multi-platform governance

---

## Off-the-Shelf LLM Gateway (LiteLLM, Portkey)

Use an existing open-source or SaaS LLM gateway rather than building one.

### Advantages

* Available today with multi-provider support
* No implementation or maintenance burden

### Disadvantages

* Limited control over extension points for future platform capabilities
* Policy, routing, evaluation, and reliability cannot be customized to the AI Control Plane's requirements
* The implementation itself is part of the learning goal of this platform

---

## Central AI Gateway (Selected)

A shared platform gateway handles provider access for all applications.

### Advantages

* Centralized operational controls
* Consistent request path
* Provider abstraction
* Shared observability foundation
* Clear extension point for policy, routing, cost, and reliability

### Disadvantages

* Introduces an additional platform dependency
* Requires strong reliability and availability
* Can become a bottleneck if poorly designed
* Requires careful API design to avoid limiting application teams

---

# Design Trade-offs

## Simplicity vs. Capability

The first implementation is intentionally small. A gateway that includes routing, policy, evaluation, observability, and cost optimization from day one risks becoming unfocused and unshippable.

v0.2 establishes the request path and extension points. Future versions add deeper capabilities incrementally.

---

## Provider Neutrality vs. Provider Features

A common OpenAI-compatible API improves portability but hides provider-specific features. The gateway supports a stable common interface while allowing controlled access to provider-specific capabilities through adapter extension points where needed.

---

## Centralization vs. Team Autonomy

Centralized access improves governance and observability. However, excessive centralization can slow application teams. The gateway makes the right path easy without preventing advanced use cases.

---

## Synchronous vs. Streaming

Synchronous is simpler to implement correctly and covers the majority of LLM use cases. Streaming improves user experience for chat interfaces but complicates every layer of the pipeline. The decision to defer streaming until v0.8 prioritizes getting the core pipeline right before introducing that complexity.

---

# Evolution Roadmap

Each version adds one coherent capability layer without changing the application-facing API contract.

```
v0.2  Foundation         — stable API, provider abstraction, static routing,
│                          metadata capture, error normalization, Java 21
│
v0.3  Observability      — queryable metadata store, token metrics,
│                          latency by provider/model/application,
│                          /v1/metrics endpoint
│
v0.4  Dynamic Routing    — control plane API for routing rules,
│                          hot-reload without restart,
│                          fallback routing on provider failure,
│                          weighted routing for model experimentation
│
v0.5  Identity & Policy  — application identity, model access policies,
│                          per-application rate limits,
│                          request allow/deny hooks, audit log
│
v0.6  Cost Management    — token cost attribution per application and team,
│                          budget limits with graceful enforcement,
│                          cost anomaly detection, chargeback export
│
v0.7  Reliability        — circuit breakers per provider,
│                          provider health checks,
│                          degraded mode responses,
│                          SLO tracking, provider failover
│
v0.8  Streaming          — SSE support on /v1/chat/completions,
│                          streaming-aware metadata and cost capture,
│                          streaming error normalization
│
v1.0  Evaluation         — request sampling for quality review,
                           response scoring hooks,
                           A/B model comparison,
                           quality trends alongside cost and latency
```

The application-facing API does not change across versions. Every new capability is invisible to applications unless they explicitly opt into it.

---

# v0.2 Reference Implementation Scope

## Include

* Spring Boot 3.x application with Java 21 virtual threads
* `POST /v1/chat/completions` (OpenAI-compatible)
* `GET /v1/models`
* `GET /health`
* `Provider` interface with mock adapter and one real adapter (Anthropic)
* `Router` interface with static YAML-driven implementation
* `MetadataRecorder` interface with async stdout JSON implementation
* Request ID generation and MDC propagation
* API key authentication filter
* Structured request/response logging
* Error normalization across all seven categories
* Graceful shutdown (drain in-flight requests on SIGTERM)
* Unit tests for router, error normalization, adapter request/response translation
* Integration tests via `MockMvc` and `WireMock`
* Dockerfile with multi-stage build
* README with local setup, environment variable reference, and example requests

## Exclude

* Streaming support
* Dynamic routing control plane
* Persistent metadata storage
* Policy engine
* Multi-tenant authorization
* Circuit breakers
* Cost tracking
* Production deployment automation

---

# Success Criteria

v0.2 is successful if:

* An application can call the gateway instead of calling a model provider directly.
* Provider-specific details are hidden behind an adapter.
* Swapping providers requires a configuration change, not an application code change.
* Every request produces a structured metadata record with request ID, application ID, provider, model, latency, and status.
* All provider errors map to normalized error types with consistent response shape.
* The mock adapter enables full end-to-end testing with no provider credentials.
* The service starts, handles concurrent requests under Java 21 virtual threads, and shuts down gracefully.
* The test suite passes with no network calls required.
* A new engineer can run the gateway locally in under five minutes following the README.

---

# Related Documents

* `VISION.md`
* `architecture/overview.md`
* `architecture-decisions/ACP-0001-why-ai-control-plane.md`
* `architecture-decisions/ACP-0002-ai-gateway.md`
* `ROADMAP.md`

---

# Summary

The AI Gateway is the front door of the AI Control Plane.

It provides the first shared operational boundary between AI applications and model providers, implemented in Java 21 with Spring Boot 3.x, exposing an OpenAI-compatible synchronous API backed by a provider abstraction layer, static alias-based routing, async metadata capture, and normalized error handling.

The initial implementation is deliberately small. Its purpose is to establish the request path and extension points — not to solve every platform concern.

Observability, dynamic routing, identity and policy, cost management, reliability, streaming, and evaluation are layered onto this foundation incrementally across future versions, without requiring application teams to change their integration.
