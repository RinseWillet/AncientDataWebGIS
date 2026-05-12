# Full-Stack Readiness Analysis Prompt (Backend + Frontend + C4 PlantUML)

Use the prompt below in a new AI coding session.

---

## Prompt

You are performing a **full-stack technical readiness assessment** for two repositories:

- Backend: `/home/r-willet/IdeaProjects/AncientDataWebGIS`
- Frontend: `/home/r-willet/IdeaProjects/AncientDataWebGIS_FE`

Current date: **May 12, 2026**.

Your goal is to determine whether both projects are **ready and up to date** to start building new features.

### Operating mode

- Be evidence-driven: every important conclusion must reference concrete files, config values, dependency versions, or test/build outputs.
- If uncertain, explicitly state assumptions and how to verify them.
- Prefer practical risks over generic best-practice advice.
- Do not refactor code unless needed to validate a claim.

### Scope

1. Analyze **backend architecture and health**.
2. Analyze **frontend architecture and health**.
3. Produce **basic PlantUML C4 diagrams** (Context + Container at minimum; Component diagram if feasible).
4. Assess **readiness for feature development** (technical debt, test confidence, CI/CD confidence, dependency posture, architecture clarity).
5. Produce a prioritized action list for next sprint.

---

## Required Deliverables

### 1) Executive Summary

Provide:
- Overall readiness score (0-100)
- Backend readiness score
- Frontend readiness score
- Top 5 blockers/risks (if any)
- "Go / Conditional Go / No-Go" recommendation

### 2) Backend Analysis

Cover at least:
- Project structure and modularity (`src/main`, `src/test`, packages)
- API layer patterns (controllers/services/repositories)
- Security posture (JWT setup, auth filters/config)
- Data layer (JPA, migrations, DB coupling)
- Configuration quality (`application*.properties`, env handling)
- Build/test health (Gradle tasks, failing tests, warnings)
- Dependency freshness/security posture (focus on critical/runtime deps)
- Maintainability risks for adding features

### 3) Frontend Analysis

Cover at least:
- Project structure and feature organization
- State management patterns (Redux/tooling usage)
- Routing and page/component boundaries
- API integration strategy (services, types, error handling)
- Build/test/lint health (Vite, TypeScript, ESLint, tests)
- Dependency freshness/security posture
- Maintainability risks for adding features

### 4) PlantUML C4 Diagrams

Provide valid PlantUML code blocks for:

- **System Context Diagram** (overall system + actors + external systems)
- **Container Diagram** (Frontend app, Backend API, Database, external integrations)
- **Optional Component Diagram** for Backend API internals (controller/service/repository/security) and Frontend feature slices if straightforward

Use simple, readable diagrams. Include short notes for major assumptions.

### 5) Readiness Decision

Answer explicitly:

- Is backend ready to start new features now?
- Is frontend ready to start new features now?
- What must be fixed first vs what can be deferred?
- What is the minimum "Day-1 feature kickoff" checklist?

### 6) Prioritized Action Plan

Create a plan with:
- Priority (P0/P1/P2)
- Task
- Repo (BE/FE/Both)
- Expected effort (S/M/L)
- Risk reduction impact

---

## Required Verification Steps (run and report)

Run these checks (adapt if commands differ):

### Backend
```bash
cd /home/r-willet/IdeaProjects/AncientDataWebGIS
./gradlew clean test
./gradlew build
```

### Frontend
```bash
cd /home/r-willet/IdeaProjects/AncientDataWebGIS_FE
npm install
npm run lint
npm run test:run
npm run build
```

If any command fails, include:
- exact failing command
- short root-cause hypothesis
- severity and impact on feature readiness

---

## Output Format

Return the final report in this exact section order:

1. Executive Summary
2. Backend Findings
3. Frontend Findings
4. C4 PlantUML Diagrams
5. Readiness Verdict
6. Priority Action Plan
7. Evidence Appendix (commands + key outputs)

Use concise Markdown. Focus on actionable findings over long prose.

---

## Quality Bar

- No vague statements like "looks fine".
- Every blocker must include concrete evidence and impact.
- Diagrams must align with actual repository structure.
- Recommendations should be realistic for immediate next sprint execution.

