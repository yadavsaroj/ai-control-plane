# AI Control Plane

> **Operating AI systems will become as important as building them.**

The AI Control Plane is an open initiative exploring the architecture, principles, and reference platform for operating production AI systems at enterprise scale.

As organizations move from a handful of AI copilots to hundreds—or eventually thousands—of AI-powered applications and autonomous agents, a new class of engineering challenges is emerging. While foundation models and agent frameworks continue to evolve rapidly, the operational layer required to run AI systems reliably at scale is still taking shape.

This repository explores what that operational layer should become.

---

## The Core Idea

Modern AI stacks provide excellent building blocks for creating intelligent applications.

What they do **not** yet provide is a shared operational platform responsible for running those applications at enterprise scale.

The AI Control Plane proposes a simple separation of concerns:

```text
AI Applications
        │
        ▼
+------------------------+
|   AI Control Plane     |
+------------------------+
| Gateway                |
| Routing                |
| Policy                 |
| Observability          |
| Evaluation             |
| Reliability            |
| Cost Management        |
+------------------------+
        │
        ▼
Foundation Models
```

Application teams build intelligent experiences.

Platform teams provide the shared operational capabilities required to make those experiences reliable, observable, secure, and cost-efficient.

---

## Repository

| Document                    | Description                        |
| --------------------------- | ---------------------------------- |
| **VISION.md**               | Why the AI Control Plane is needed |
| **architecture/**           | Reference architecture             |
| **architecture-decisions/** | AI Control Plane Proposals (ACPs)  |
| **implementation/**         | Reference platform                 |
| **leadership/**             | Building AI platform organizations |
| **notes/**                  | Research notebook                  |

---

## Start Here

1. **VISION.md**
2. **architecture/overview.md**
3. **architecture-decisions/ACP-0001-why-ai-control-plane.md**

---

## Current Status

**Release:** v0.1.0 – Foundation

Current work focuses on defining the architectural foundations of the AI Control Plane.

The next milestone is the design and implementation of the AI Gateway.

---

## License

MIT
