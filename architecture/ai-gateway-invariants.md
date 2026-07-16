# AI Gateway Architectural Invariants

**Status:** Draft
**Version:** 0.1
**Last Updated:** July 2026

---

## Purpose

This document defines the architectural invariants for the AI Gateway.

Invariants are rules that should remain true as the gateway evolves. They are intended to preserve clean boundaries, prevent accidental coupling, and ensure that future capabilities such as routing, policy, observability, reliability, cost management, and evaluation can be added without breaking the application contract.

---

## Core Invariant

The application-facing API must remain stable as the gateway evolves.

Applications should not need to change their integration when new platform capabilities are added behind the gateway.

This is the primary reason the gateway exists.

---

## Invariants

### 1. Applications never call providers directly

All application traffic must flow through the AI Gateway.

Provider-specific integration details are owned by the gateway, not by application teams.

---

### 2. Controllers never call provider implementations directly

Controllers may depend on abstractions such as `Router`, `ProviderRegistry`, and `MetadataRecorder`.

Controllers must not instantiate or invoke concrete provider adapters directly.

---

### 3. Provider-specific DTOs never leave the provider package

Provider request and response formats are internal implementation details.

The gateway core model is the only model shared across routing, providers, metadata, and API orchestration.

---

### 4. Provider adapters own translation

Each provider adapter is responsible for translating between the gateway core model and the provider-specific wire format.

No provider translation logic should exist in controllers, routers, middleware, or metadata code.

---

### 5. Routing is independent of provider implementation

The router selects a provider and model.

It must not know how a provider is implemented or how the provider invokes its backend API.

---

### 6. New providers require no controller changes

Adding a new provider should require:

* a provider adapter
* provider registration
* routing configuration

It should not require changes to controller logic.

---

### 7. Metadata capture is non-blocking

Request metadata must not block the request path.

If metadata recording fails, the gateway should preserve request availability and surface the failure through operational logs or metrics.

---

### 8. Metadata is captured for both success and failure

Every request should produce metadata regardless of outcome.

At minimum, metadata should include:

* request ID
* application identity, when available
* provider
* model
* latency
* status
* error category, when applicable
* timestamp

---

### 9. All provider errors are normalized

Provider-specific failures must be converted into gateway-defined error categories.

Applications should receive stable error types rather than provider-specific error formats.

---

### 10. Gateway errors include request identity

Every error response should include a request ID.

This allows application teams and platform operators to correlate user-visible failures with gateway logs and metadata.

---

### 11. Providers are stateless

Provider adapters should not maintain request-specific state between invocations.

Stateful behavior such as routing history, rate limiting, cost tracking, or circuit breaker state belongs in dedicated platform components.

---

### 12. Middleware handles cross-cutting request concerns only

Middleware may handle request ID generation, authentication, and request-level logging.

Middleware must not contain routing, provider translation, model selection, policy business logic, or provider invocation logic.

---

### 13. Configuration is consumed at the boundary

Configuration should be loaded and validated at startup or through explicit control-plane mechanisms.

Core request handling should depend on typed configuration objects, not raw configuration parsing.

---

### 14. The gateway data plane and control plane evolve independently

The data plane API used by applications should remain stable.

Control plane capabilities such as route updates, policy changes, model aliases, and budgets may evolve independently behind that stable data plane contract.

---

### 15. Future capabilities must attach through explicit extension points

Observability, policy, cost management, evaluation, and reliability should integrate through explicit interfaces or well-defined hooks.

They should not require rewriting the request flow.

---

## Dependency Invariants

The package dependency rules are part of the architecture.

### Foundation packages

`core` and `error` should remain foundational.

They must not depend on framework, provider, router, middleware, metadata, or configuration packages.

### API package

`api` may orchestrate requests by depending on abstractions.

It must not depend on concrete provider implementations.

### Provider package

`provider` may depend on `core` and `error`.

It must not depend on `api`, `router`, `middleware`, or `metadata`.

### Router package

`router` may depend on `core` and `error`.

It must not depend on `provider` implementations, `middleware`, or `metadata`.

### Metadata package

`metadata` should depend only on stable core concepts.

It must not depend on controllers, providers, routers, or middleware.

---

## Enforcement

In v0.2, these invariants are enforced through code review.

In future versions, they should be enforced through automated tests, such as dependency-rule tests or architecture tests.

Potential future enforcement mechanisms include:

* ArchUnit tests
* package visibility restrictions
* static analysis
* build-time dependency checks
* integration tests validating provider isolation

---

## Why Invariants Matter

The AI Gateway will evolve across multiple releases.

Future versions may add:

* dynamic routing
* policy enforcement
* cost attribution
* circuit breakers
* streaming
* evaluation hooks
* persistent observability

Without architectural invariants, these capabilities can gradually erode boundaries and turn the gateway into a tightly coupled proxy.

The invariants in this document are intended to keep the gateway extensible, operable, and understandable as it grows.

---

## Related Documents

* `architecture/ai-gateway.md`
* `architecture/overview.md`
* `architecture-decisions/ACP-0002-ai-gateway.md`
* `VISION.md`

