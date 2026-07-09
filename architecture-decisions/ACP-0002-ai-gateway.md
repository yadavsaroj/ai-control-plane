# ACP-0002: AI Gateway

**Status:** Accepted for v0.2
**Version:** 0.2
**Author:** Saroj Yadav
**Created:** July 2026

---

# Summary

This proposal establishes the **AI Gateway** as the first production capability of the AI Control Plane.

The gateway provides a stable, provider-neutral interface between AI applications and foundation model providers. More importantly, it establishes the shared operational boundary upon which future AI Control Plane capabilities—including observability, routing, identity, policy, reliability, cost management, and evaluation—will be built.

The gateway is intentionally minimal in v0.2. Its primary purpose is to prove the architectural abstraction rather than maximize functionality.

---

# Context

The AI Control Plane is envisioned as the operational platform for production AI systems.

Many platform capabilities have been identified, including:

* AI Gateway
* Observability
* Dynamic Routing
* Identity & Policy
* Cost Management
* Reliability Engineering
* Evaluation
* Incident Management

The question addressed by this proposal is:

> **Which capability should be implemented first?**

Rather than attempting to build multiple capabilities simultaneously, the platform should evolve incrementally, with each release introducing one coherent operational capability.

---

# Problem Statement

Today, most AI applications integrate directly with model providers.

While this approach is simple, it creates several long-term challenges:

* Provider-specific APIs become embedded within application code.
* Authentication and credential management are duplicated across teams.
* Routing logic becomes application-specific.
* Operational telemetry is fragmented.
* Policy enforcement is inconsistent.
* Provider migration becomes expensive.
* Cross-application governance is difficult.

As organizations scale AI adoption, these concerns become platform problems rather than application problems.

A shared operational boundary is required.

---

# Decision

The AI Gateway will be the first implemented capability of the AI Control Plane.

Applications will communicate with the gateway rather than directly with foundation model providers.

The gateway will expose a stable, OpenAI-compatible API while abstracting provider-specific implementation details behind a provider adapter interface.

Future operational capabilities will extend the gateway without requiring application teams to change their integrations.

---

# Why the AI Gateway Comes First

The gateway establishes the request path through which nearly every operational capability can evolve.

For example:

| Capability      | Depends on Gateway                                            |
| --------------- | ------------------------------------------------------------- |
| Observability   | Request metadata originates in the gateway                    |
| Dynamic Routing | Routing decisions occur in the gateway                        |
| Identity        | Authentication terminates at the gateway                      |
| Policy          | Requests are evaluated at the gateway                         |
| Cost Management | Usage is attributed at the gateway                            |
| Reliability     | Retries, failover, and circuit breakers belong in the gateway |
| Evaluation      | Sampling and quality signals originate from gateway traffic   |

Without a shared request path, every capability would require separate integrations across every AI application.

The gateway creates a single operational boundary through which the platform can evolve incrementally.

---

# Architectural Principles

This decision is guided by five principles.

## Stable Data Plane

Applications integrate once.

The application-facing API should remain stable as the platform evolves.

---

## Evolving Control Plane

Operational capabilities evolve behind the gateway without affecting applications.

---

## Provider Neutrality

Applications depend on gateway abstractions rather than provider-specific APIs.

---

## Incremental Evolution

The platform grows by adding one coherent capability layer at a time.

---

## Statelessness

Gateway instances remain stateless and horizontally scalable.

Operational state belongs outside the request path.

---

# Alternatives Considered

## Alternative 1 — Direct Provider Integration

Each application integrates directly with foundation model providers.

### Advantages

* Simple implementation
* No additional infrastructure
* Full provider flexibility

### Disadvantages

* Duplicated integration logic
* Fragmented observability
* Difficult governance
* Provider lock-in
* Inconsistent operational behavior

---

## Alternative 2 — Build Observability First

Implement centralized telemetry before introducing a gateway.

### Advantages

* Immediate operational visibility
* Minimal application disruption

### Disadvantages

* No shared request path
* Limited ability to influence operational behavior
* Routing and policy remain fragmented

Observability without a common request path provides insight but limited control.

---

## Alternative 3 — Build Policy First

Implement centralized authorization before introducing provider abstraction.

### Advantages

* Early governance
* Improved security posture

### Disadvantages

* Policy still requires application-specific integrations
* Operational behavior remains inconsistent

Policy becomes significantly simpler once requests flow through a common gateway.

---

## Alternative 4 — AI Gateway (Selected)

Introduce a centralized gateway that manages all model access.

### Advantages

* Stable application contract
* Provider abstraction
* Shared operational boundary
* Centralized authentication hooks
* Foundation for future capabilities
* Simplified provider migration

### Disadvantages

* Additional infrastructure component
* Must maintain high availability
* Adds one network hop

These trade-offs are justified by the long-term architectural flexibility gained.

---

# Consequences

If accepted:

* Applications integrate with the AI Gateway rather than individual providers.
* Provider-specific logic is isolated behind adapters.
* Operational capabilities can evolve independently of application code.
* Future platform releases preserve the application-facing API while extending operational behavior.

This proposal establishes the design philosophy for the AI Control Plane:

> **Build shared operational boundaries first. Add operational capabilities incrementally behind stable interfaces.**

---

# v0.2 Scope

The first implementation intentionally remains small.

Included:

* OpenAI-compatible API
* Provider abstraction
* Static routing
* API key authentication
* Structured request metadata
* Error normalization
* Basic retry and timeout handling
* Mock provider
* One production provider adapter

Deferred:

* Streaming
* Dynamic routing
* Policy engine
* Cost management
* Circuit breakers
* Evaluation
* Persistent telemetry
* Multi-tenancy

---

# Future Evolution

The gateway evolves through coherent capability layers.

```text
v0.2  Foundation
        │
v0.3  Observability
        │
v0.4  Dynamic Routing
        │
v0.5  Identity & Policy
        │
v0.6  Cost Management
        │
v0.7  Reliability
        │
v0.8  Streaming
        │
v1.0  Evaluation
```

Each release extends the platform behind a stable application-facing API.

---

# Success Criteria

This proposal is successful if:

* Applications integrate with the gateway rather than providers.
* Provider-specific details remain hidden behind adapters.
* Future platform capabilities can be added without changing application integrations.
* The reference implementation validates the architectural abstraction defined by the AI Control Plane.

---

# Related Documents

* `VISION.md`
* `ROADMAP.md`
* `architecture/overview.md`
* `architecture/ai-gateway.md`
* `ACP-0001 — Why the AI Control Plane?`

---

# Status

**Accepted for v0.2**

This proposal establishes the AI Gateway as the foundational capability of the AI Control Plane and defines the architectural principles that guide its implementation and future evolution.
