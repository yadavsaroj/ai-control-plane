# ACP-0002: AI Gateway

**Status:** Draft
**Version:** 0.1
**Author:** Saroj Yadav
**Created:** June 2026

---

# Summary

This proposal establishes the **AI Gateway** as the first production capability of the AI Control Plane.

The gateway provides a stable, provider-neutral entry point for AI applications and establishes the operational boundary between applications and AI infrastructure.

Rather than integrating directly with model providers, AI applications communicate through the gateway, enabling future platform capabilities—including routing, policy enforcement, observability, reliability, evaluation, and cost management—to be introduced without requiring application changes.

---

# Context

The AI Control Plane defines the shared operational layer responsible for operating production AI systems.

Many capabilities have been identified as part of this platform, including:

* Model Routing
* Identity & Policy
* Observability
* Evaluation
* Reliability Engineering
* Cost Management
* Capacity Planning
* Incident Management

The question is not **whether** these capabilities are valuable.

The question is **where to begin.**

This proposal argues that the AI Gateway should be implemented first because it establishes the request path through which every future operational capability can be introduced.

---

# Problem Statement

Today, most AI applications communicate directly with model providers.

While simple, this approach introduces several challenges:

* Provider-specific APIs become embedded in application code.
* Authentication is managed independently by each application.
* Operational visibility is fragmented.
* Cost attribution becomes inconsistent.
* Policy enforcement is difficult to centralize.
* Provider migration requires application changes.
* Reliability mechanisms vary across teams.

As organizations scale AI adoption, these inconsistencies increase operational complexity.

A shared operational boundary is needed.

---

# Proposal

Introduce an **AI Gateway** as the first production capability of the AI Control Plane.

All AI requests should pass through the gateway before reaching foundation models.

The gateway will initially provide:

* Stable request API
* Provider abstraction
* Static routing
* Request metadata collection
* Error normalization
* Authentication hooks
* Basic retry and timeout handling

Future platform capabilities will extend the gateway rather than requiring application teams to modify their integrations.

---

# Why the AI Gateway Comes First

Every subsequent AI Control Plane capability depends on understanding or influencing request flow.

For example:

* **Model Routing** selects where requests are sent.
* **Policy Enforcement** determines whether requests are allowed.
* **Observability** measures request behavior.
* **Evaluation** captures request quality.
* **Reliability Engineering** manages retries and failover.
* **Cost Management** attributes usage.
* **Incident Management** correlates failures.

Without a shared request path, each capability would need to integrate independently with every application.

The gateway establishes a single operational boundary through which these capabilities can evolve incrementally.

---

# Alternatives Considered

## Alternative 1 — Direct Provider Integration

Each application integrates directly with model providers.

### Advantages

* Simple for prototypes
* No platform dependency
* Full provider flexibility

### Disadvantages

* Repeated implementation effort
* Inconsistent operational behavior
* Difficult governance
* Fragmented observability
* Strong provider coupling

---

## Alternative 2 — Build Observability First

Create centralized telemetry before introducing a gateway.

### Advantages

* Immediate operational visibility
* Minimal application disruption

### Disadvantages

* Limited ability to enforce operational behavior
* Difficult to standardize request handling
* Routing and policy remain application-specific

Observability without a shared request path provides insight but limited control.

---

## Alternative 3 — Build Policy First

Introduce centralized governance before provider abstraction.

### Advantages

* Earlier compliance capabilities
* Strong security posture

### Disadvantages

* Policy enforcement still requires application-specific integrations
* Operational consistency remains fragmented

Policy becomes significantly simpler once requests flow through a common gateway.

---

## Alternative 4 — AI Gateway (Proposed)

Centralize model access behind a shared platform gateway.

### Advantages

* Provider-neutral application interface
* Consistent operational behavior
* Foundation for future platform capabilities
* Incremental evolution
* Simplified provider migration
* Clear ownership boundary

### Disadvantages

* Introduces an additional platform dependency
* Requires high availability
* Adds an extra network hop

These trade-offs are outweighed by the long-term operational benefits.

---

# Architectural Decision

The AI Gateway will become the **first production capability** of the AI Control Plane.

Its initial responsibilities are intentionally limited.

The gateway should focus on establishing a stable operational boundary rather than solving every platform concern.

Additional capabilities—including routing, policy, observability, evaluation, and reliability—will be layered onto this foundation over time.

---

# Consequences

If accepted:

* AI applications integrate with the gateway rather than model providers.
* Provider-specific logic becomes isolated behind adapters.
* Future operational capabilities build upon an existing request path.
* The AI Control Plane evolves incrementally rather than requiring a large, monolithic implementation.

This proposal also establishes a design philosophy for the project:

> **Build shared operational boundaries first. Add platform capabilities incrementally.**

---

# Open Questions

Several design decisions remain intentionally open:

* Should the gateway expose an OpenAI-compatible API, a native AI Control Plane API, or both?
* Should request metadata be captured synchronously or asynchronously?
* Where should authentication terminate?
* What routing strategy should become the default?
* Which provider should be implemented first?
* How should streaming responses be handled?

These questions will be addressed in future proposals and implementation milestones.

---

# Evolution Strategy

The AI Gateway is designed to evolve incrementally while preserving a stable application interface.

Applications should never need to change their integration as the gateway gains new capabilities. Instead, new operational capabilities are layered onto the existing request path.

The planned evolution is:

v0.2 — Foundation
- Provider abstraction
- Static routing
- Basic retries
- Error normalization

v0.3 — Observability
- Structured request metadata
- Metrics
- Searchable request history

v0.4 — Identity & Policy
- Application identity
- Authorization
- Rate limiting
- Audit

v0.5 — Dynamic Routing
- Hot-reload routing
- Weighted routing
- Failover
- Model aliases

...

This incremental approach allows the platform to mature without requiring application changes.

The application contract remains stable:

POST /v1/chat/completions

# Related Documents

* `VISION.md`
* `ROADMAP.md`
* `architecture/overview.md`
* `architecture/ai-gateway.md`
* `ACP-0001 — Why the AI Control Plane?`

---

# Decision

**Accepted for v0.2**

The AI Gateway is the first capability to be implemented because it establishes the shared operational boundary upon which the rest of the AI Control Plane can be built.

