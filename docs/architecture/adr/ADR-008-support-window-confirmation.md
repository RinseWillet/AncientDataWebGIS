# ADR-008: Spring Boot / Java Support-Window Confirmation (Spike 1)

**Status:** Proposed
**Date:** 2026-07-27
**Decision makers:** Backend team
**Epic/Story:** Spring Boot 4 + Java 25 upgrade epic — Spike 1 (version/support confirmation)

---

## Context

Before scheduling the real implementation (per the technical spikes on branches spike/spring-boot-4, spike/java-25-toolchain, and spike/boot4-java25-combined, all passed), this spike confirms the *official support timelines* live from endoflife.date and Spring's own project data, since these move fast and hadn't been verified with a live source until now.

**Data pulled live on 2026-07-27** from `endoflife.date/api/spring-boot.json`, `endoflife.date/api/spring-framework.json`, and `endoflife.date/api/eclipse-temurin.json`.

---

## Decision / Key Findings

### ⚠️ Our current version (Spring Boot 3.5.x) is already past free OSS support

| Cycle | Released | **OSS EOL** | Extended (commercial) support until | Notes |
|---|---|---|---|---|
| **3.5** (current) | 2025-05-31 | **2026-06-30** ⚠️ **already passed** (today: 2026-07-27) | 2032-06-30 | This project is on 3.5.16 |
| 4.0 | 2025-11-30 | 2026-12-31 | 2027-12-31 | First 4.x release |
| **4.1** (latest, used in spikes) | 2026-06-30 | 2027-07-31 | 2028-07-31 | Supports Java 17–26 |

**This means free community (OSS) support for Spring Boot 3.5.x ended about 4 weeks ago.** The project isn't in immediate danger (no support contract was relied upon, and 3.5.16 still functions correctly), but it does mean:
- No further 3.5.x patch releases should be expected from the open-source project (only under a paid Spring/Broadcom support contract).
- Any newly discovered CVE in the 3.5.x line will not receive a free-tier fix — this raises the practical urgency of the upgrade beyond "nice to have."

### Spring Boot has no "LTS" tier (confirmed)

Every cycle in the data — including 3.5 and 4.1 — is marked `"lts": false`. Spring doesn't use LTS branding; each minor gets ~13 months of free OSS support followed by ~12–72 months of paid extended support. **4.1.0 (the version used in the spikes) currently has the longest remaining free OSS runway** (until 2027-07-31) of any available option.

### Java 25 / Eclipse Temurin — confirmed LTS, good runway

| | |
|---|---|
| Release date | 2025-09-22 |
| **LTS** | ✅ `true` |
| EOL (Temurin community support) | **2031-09-30** |
| Latest patch at spike time | 25.0.3+9 (matches what's installed locally, confirmed on branch spike/java-25-toolchain) |

No concerns here — Java 25 is a genuine, well-supported LTS with ~5 years of runway remaining.

### Spring Framework 7.0 (underlies Boot 4.1) — consistent picture

Released 2025-11-30, supports Java 17–25, OSS EOL 2027-07-31, extended support to 2028-07-31 — consistent with Boot 4.1's own timeline (Boot wraps Framework).

---

## Alternatives Considered

### A. Stay on Spring Boot 3.5.x a while longer, rely on extended/commercial support if needed
- Rejected as the primary path — extended support requires a paid Spring/Broadcom contract; free security patches have already stopped. Given the spikes on branches spike/spring-boot-4, spike/java-25-toolchain, and spike/boot4-java25-combined already show Boot 4.1 + Java 25 upgrade is low-risk and mostly mechanical, there's no strong reason to stay.

### B. Target Spring Boot 4.0.x instead of 4.1.0
- Rejected — 4.0.x OSS support ends sooner (2026-12-31, ~5 months away) than 4.1.x (2027-07-31). 4.1.0 is both the newest and the longest-supported option; no reason to pick the older minor.

---

## Consequences

### Positive

- Confirms 4.1.0 (already validated on branches spike/spring-boot-4 and spike/boot4-java25-combined) is the right target — newest, longest OSS support window, and no additional risk found in the technical spikes.
- Confirms Java 25/Temurin is a safe, long-runway LTS choice, independently verified.

### Negative

- Reveals the current production stack (Boot 3.5.16) has been outside free OSS support since 2026-06-30 — this should be flagged to stakeholders as a reason to prioritize this upgrade sooner rather than later, not just as exploratory backlog work.

### When to Revisit

- If the upgrade is deferred, re-check endoflife.date periodically — 4.1.x's own OSS window ends 2027-07-31.

---

## Implementation References

- Live data sources: `https://endoflife.date/api/spring-boot.json`, `https://endoflife.date/api/spring-framework.json`, `https://endoflife.date/api/eclipse-temurin.json` (queried 2026-07-27).
- Related spike branches (not merged): spike/spring-boot-4 (Boot 4.1.0 alone), spike/java-25-toolchain (Java 25 alone), spike/boot4-java25-combined (combined trial). Each records its own ADR-008/009/010 on its branch (branch-local numbering, to be reconciled at real-implementation time).

## Updated Recommendation

All four spikes (this one plus the three technical spikes above) are now complete with no blockers found. Combined with the discovery that Boot 3.5.x OSS support has already lapsed, recommend **elevating this from exploratory backlog to a scheduled near-term Epic**:

1. Implementation story: combined Boot 4.1.0 + Java 25/Temurin bump, test-module dependency fix, `Dockerfile` base image swap (per the combined spike branch findings).
2. Verification story: live smoke test against a real PostGIS instance (dev/staging) before merging.

