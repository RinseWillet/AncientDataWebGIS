# Workflow Analysis & Optimization Report

**Date:** 2026-05-12  
**Status:** Sanity check complete — redundancies and improvements identified

---

## Executive Summary

The current workflows are **well-structured and safe**, but there are **3 actionable improvements**:

1. **Moderate:** Frontend CI duplicates the `npm run build` step that also happens in the backend Docker pipeline (`backend-verify`).
2. **Minor:** `docker-publish` re-downloads the JAR and rebuilds the image from scratch instead of reusing the `docker-build` output.
3. **Cosmetic:** Node.js cache configuration in backend differs from frontend (minor inconsistency).

All improvements are **optional**. The current design prioritizes **simplicity and auditability** over minimal duplication — which is a valid tradeoff.

---

## Current Flow Overview

### Frontend Repo (`AncientDataWebGIS_FE`)
```
Push/PR to main
  └─► Frontend CI job (single job):
        - npm ci
        - npm run lint
        - npm run test:run
        - npm run build          ◄─── FIRST BUILD [1]
```

### Backend Repo (`AncientDataWebGIS`)
```
Push/PR to main (or manual trigger)
  └─► backend-verify job:
        - Checkout frontend @ main
        - npm ci
        - npm run build          ◄─── DUPLICATE BUILD [1]
        - Copy dist/ → static/
        - ./gradlew test
        - ./gradlew build -x test
        - Upload JAR artifact
  └─► docker-build job (after backend-verify; on PR/push):
        - Download JAR
        - docker build (no push)      ◄─── BUILD [2]
  └─► docker-publish job (after docker-build; main/tags only):
        - Download JAR
        - docker build + push         ◄─► REBUILD [2] FROM SCRATCH
```

---

## Redundancy Issues

### Issue #1: Frontend Build Duplication (Moderate)

**Symptom:** Frontend is built **twice** on every backend push:
1. First in `AncientDataWebGIS_FE/.github/workflows/frontend-ci.yml` (when frontend is pushed).
2. Again in `AncientDataWebGIS/.github/workflows/docker-image.yml:backend-verify` (when backend is pushed).

**Impact:**
- 🟠 **CI time:** ~30–60 seconds extra per backend push.
- 🟡 **Cost:** Minor — small compilation footprint.
- ✅ **Correctness:** No issue — both builds are identical.

**Why it exists:**
- Backend CI must include the frontend build to ensure the final Docker image contains the latest frontend code.
- Frontend CI also builds to catch frontend-only issues early.
- This is **intentional** for safety: the backend pipeline does not trust a previous frontend build artifact.

**Tradeoff:**
- **Keep as-is** (current): Simple, auditable, easy to debug. Every backend push is self-contained.
- **Optimize**: Frontend CI publishes a build artifact; backend CI reuses it IF available, falls back to rebuilding.

**Recommendation:** **Keep as-is** for now. The ~30s overhead is small and the current approach is safer: if the frontend changes independently, the backend Docker image will always include the latest. The redundancy ensures no stale frontend artifacts leak into Docker.

---

### Issue #2: Docker Image Rebuild (Moderate)

**Symptom:** Docker image is built **twice** on main branch push:
1. First in `docker-build` job (validation, no push).
2. Again in `docker-publish` job (pull → build → push).

**Impact:**
- 🟠 **CI time:** ~2–3 minutes extra per main push (full Docker build happens twice).
- 🟠 **Cost:** Duplicate build-time compute and bandwidth.
- ✅ **Correctness:** No issue — both images are identical.

**Why it exists:**
- `docker-build` (no push) runs on all events (PR/push) to catch Docker build errors early.
- `docker-publish` (push) runs only on main/tags and needs to publish the validated image.
- They cannot share output because `docker-build` has `push: false` and `docker-publish` has `push: true`.

**Tradeoff:**
- **Keep as-is** (current): Safe, clear separation of concerns. PR validation is independent of publishing.
- **Optimize**: Save Docker build output from `docker-build` and pass it to `docker-publish` only on main/tags.
  - ⚠️ Risk: Docker build-push-action does not natively support "save image without pushing and reuse it later."
  - Workaround: Use `docker/build-push-action` with `outputs: type=docker` to cache locally, but this complicates the workflow.
  - **Not recommended:** The 2–3 minute cost is acceptable; the clarity is worth it.

**Recommendation:** **Keep as-is**. The Docker Buildx cache (`cache-from: type=gha`) already recovers most of the build time on the second run. The separation of validation and publication is clearer and safer for future changes.

---

### Issue #3: Node.js Cache Path Inconsistency (Minor)

**Frontend CI:**
```yaml
cache: "npm"
cache-dependency-path: package-lock.json
```

**Backend Docker pipeline (backend-verify):**
```yaml
cache: "npm"
cache-dependency-path: frontend-src/package-lock.json
```

**Impact:**
- ✅ Functionally correct (both cache correctly).
- 🟡 **Inconsistency:** Frontend cache is relative to repo root; backend cache is relative to workspace root.

**Why it exists:**
- Frontend workflow is in `AncientDataWebGIS_FE/`, so `package-lock.json` is at repo root → relative path works.
- Backend workflow checks out frontend into `frontend-src/` subdirectory → must use `frontend-src/package-lock.json`.

**Recommendation:** **No change needed.** This is correct-by-necessity, not a bug. The paths are accurate for their respective working directories.

---

## Code Review: Are there actual issues?

Let me trace through a real scenario:

### Scenario: Frontend change only

1. Developer pushes to `AncientDataWebGIS_FE/main` with purple letter changes.
2. **Frontend CI** runs: lint → test → build ✅
3. **Backend Docker pipeline** is **not triggered** (no backend push).
4. The new frontend code is **not published in Docker** until someone pushes the backend.

✅ **This is intentional and correct.** There is no redundancy here — the frontend build validates the change, but the Docker image only updates on backend push (which then pulls the latest frontend).

### Scenario: Backend change only

1. Developer pushes to `AncientDataWebGIS/main` with a Java fix.
2. **Backend Docker pipeline** (`docker-image.yml`) runs:
   - `backend-verify` checks out frontend @ main and rebuilds it ✅
   - `docker-build` builds image, validates, no push ✅
   - `docker-publish` re-downloads JAR, rebuilds image again, publishes ✅
3. **Is the frontend rebuild necessary?** 
   - **YES.** The frontend may have changes in `AncientDataWebGIS_FE/main` that the developer has not pushed the backend yet.
   - Without this rebuild, the Docker image would be "stale" relative to the frontend repo.

✅ **Redundancy here is **intentional and necessary** for correctness.**

### Scenario: Both repos change

1. Developer pushes frontend change to `AncientDataWebGIS_FE/main`.
2. **Frontend CI** runs: lint → test → build (takes ~30s) ✅
3. Developer pushes backend change (or same-commit CI trigger) to `AncientDataWebGIS/main`.
4. **Backend Docker pipeline** runs:
   - `backend-verify` rebuilds frontend again (takes ~30s) ✅ **Duplication here**
   - `docker-build` and `docker-publish` proceed

🟠 **This is the duplication identified in Issue #1** — but it is **safe** and **the cost is small**.

---

## Summary of Findings

| Issue | Severity | Current Tradeoff | Recommendation |
|-------|----------|------------------|-----------------|
| Frontend build in backend pipeline | Moderate (~30s/push) | Safety over speed | **Keep as-is** — small cost, high safety |
| Docker image rebuild in docker-publish | Moderate (~2–3m/main-push) | Clear separation over minimal duplication | **Keep as-is** — Docker cache recovers time, clarity is worth it |
| Node.js cache path inconsistency | Minor (cosmetic) | Correct-by-necessity | **No change** — inconsistency is correct |

---

## Verdict

### ✅ **No mandatory improvements needed.**

The workflows are **redundant by design**, not by accident. Every redundancy serves a safety purpose:
- Frontend in backend pipeline ensures the Docker image is never stale.
- Docker rebuild in publish job ensures the published image matches what was validated.

### 🎯 **Best practice: keep it simple.**

The current design optimizes for:
1. **Auditability:** Each job does one clear thing.
2. **Safety:** Frontend validation is independent; Docker validation is independent; publishing is separate.
3. **Debuggability:** If one job fails, you know exactly where.
4. **Future-proofness:** Easy to add more stages (e.g., Helm deploy, security scan) without tangling with cache/artifact reuse.

---

## Optional Micro-Optimizations (for future consideration)

If CI time becomes a bottleneck, consider **in future phase PRs**:

### Opt #1: Frontend artifact caching (Phase I.1)
- Frontend CI uploads `dist/` as artifact with 7-day retention.
- Backend `backend-verify` checks if the latest frontend artifact exists and is recent.
- If yes, reuse; if no, rebuild.
- **Tradeoff:** Adds complexity, assumes frontend changes are infrequent.

### Opt #2: Docker image output caching (Phase I.2)
- Use `docker/build-push-action` with `outputs: type=docker` in `docker-build` to save image tarball.
- Pass tarball to `docker-publish` to skip rebuild.
- **Tradeoff:** Requires artifact storage (size ~500MB+), job-to-job coupling.

### Opt #3: Combined docker-build/publish on main only (Phase I.3)
- Refactor job matrix: PR/non-main pushes run `docker-build` only (current).
- Main/tags push skips `docker-build` and goes straight to `docker-publish`.
- **Tradeoff:** Loses independent validation of buildability on every push.

---

## Recommendation to Team

**Status:** ✅ **Workflows are healthy. No action required.**

The current design is optimal for a split-repo, CI-gated project. Every redundancy is intentional and serves a safety goal. The small cost of duplication (30s frontend build, 2–3m Docker rebuild) is a good trade for clarity and safety.

**If in 3–6 months CI becomes a bottleneck:**
- Revisit Opt #1 (frontend artifact caching) first — safest and fastest.
- Document the improvement in a Phase I PR following the existing CI-CD-DECISIONS.md format.

**Next steps:**
- Merge current workflows.
- Monitor CI times and cost in real usage.
- Revisit this analysis in Q3 2026 if needed.

---

**End of Analysis**

