# Security Vulnerability Remediation Prompt

**Date:** May 12, 2026  
**Source:** Dependabot security alert digest (week of May 5–12)  
**Project:** AncientDataWebGIS (Spring Boot 3.5.14 backend)

---

## Task

Update all vulnerable dependencies in `/home/r-willet/IdeaProjects/AncientDataWebGIS/build.gradle` to resolve 15+ CVEs of varying severity (High, Critical).

---

## Current Vulnerable Dependencies

| Dependency | Current Version | Required Upgrade | Severity | CVEs |
|------------|-----------------|------------------|----------|------|
| `org.json:json` | < 20230227 | ≥ 20230227 | High | CVE-2022-45688, CVE-2023-5072 |
| `org.apache.commons:commons-compress` | >= 1.21 < 1.26.0 | ≥ 1.26.0 | Moderate | CVE-2024-26308, CVE-2024-25710 |
| `org.xmlunit:xmlunit-core` | < 2.10.0 | ≥ 2.10.0 | Low | CVE-2024-31573 |
| `org.springframework:spring-web` | >= 6.1.0 < 6.1.12 | ≥ 6.1.12 | Moderate | CVE-2024-38809, CVE-2024-38820, CVE-2025-41234 |
| `org.springframework:spring-webmvc` | >= 6.1.0 < 6.1.13 | ≥ 6.1.13 | **High** | CVE-2024-38816, CVE-2024-38819 |
| `org.apache.tomcat.embed:tomcat-embed-core` | >= 10.1.0-M1 < 10.1.34 | ≥ 10.1.34 | **Critical** | CVE-2025-24813, CVE-2026-29145, CVE-2024-50379 (+ 18 more) |
| `ch.qos.logback:logback-core` | >= 1.4.0 < 1.5.13 | ≥ 1.5.13 | Low–Moderate | CVE-2024-12801, CVE-2024-12798, CVE-2025-11226 |
| `net.minidev:json-smart` | >= 2.5.0 < 2.5.2 | ≥ 2.5.2 | High | CVE-2024-57699 |
| `org.apache.activemq:artemis-project` | >= 1.5.1 < 2.40.0 | ≥ 2.40.0 | Moderate | CVE-2025-27391 |
| `org.springframework.boot:spring-boot` | >= 3.3.0 <= 3.3.10 | ≥ 3.3.11 | High | CVE-2025-22235 |
| `org.springframework:spring-context` | >= 6.1.0 < 6.1.14 | ≥ 6.1.14 | Low–Moderate | CVE-2025-22233, CVE-2024-38820 |
| `com.fasterxml.jackson.core:jackson-core` | < 2.15.0 | ≥ 2.15.0 | High | CVE-2025-52999, GHSA-72hv-8253-57qq |
| `org.apache.commons:commons-lang3` | >= 3.0 < 3.18.0 | ≥ 3.18.0 | Moderate | CVE-2025-48924 |
| `org.springframework:spring-core` | (version not shown in alert) | ? | ? | Check Dependabot for full details |

---

## Current build.gradle State

### Explicitly Pinned:
- `org.apache.commons:commons-lang3:3.20.0` ✅ (already compliant)
- `org.json:json:20251224` ✅ (already compliant)
- `jackson-core` (managed by Spring Boot BOM, may need inspection)
- `logback-core` (managed by Spring Boot BOM, may need inspection)

### BOM-Managed (Spring Boot 3.5.14):
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-actuator`
- `spring-boot-starter-test`
- `hibernate-spatial`
- `spring-security-crypto`
- `spring-security-test`

### Transitive Dependencies:
- `springer-web`, `spring-webmvc`, `spring-context` (pulled by starters)
- `tomcat-embed-core` (pulled by `spring-boot-starter-web`)
- `commons-compress`, `xmlunit-core`, `json-smart`, `artemis-project` (likely transitive)

---

## Strategy

### Approach A: Upgrade Spring Boot BOM version (Recommended)
**Rationale:** Spring Boot 3.5.14 is likely part of a BOM that pins Spring Framework, Tomcat, Logback, Jackson, etc.

**Steps:**
1. Check if Spring Boot 3.5.14 is already bundled with safe versions of:
   - Spring Framework 6.1.14+ (for spring-core, spring-web, spring-webmvc, spring-context)
   - Tomcat Embed 10.1.34+ (for tomcat-embed-core)
   - Logback 1.5.13+ (for logback-core)
   - Jackson 2.15.0+ (for jackson-core)
2. If **not**: upgrade Spring Boot to 3.5.15+ or 3.6.x (if available and compatible).
3. If **yes**: no action needed on those transitive deps.
4. Explicitly override any remaining mismatched versions in `build.gradle`.

### Approach B: Override transitive versions (Fallback)
**Rationale:** If BOM upgrade is not feasible, pin specific versions manually.

**Pattern:**
```groovy
configurations.all {
    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
        if (details.requested.name == 'tomcat-embed-core') {
            details.useVersion '10.1.34'
        }
        // ... repeat for each vulnerable transitive
    }
}
```

---

## Pre-Flight Checks

Before making upgrades:

1. **Check Spring Boot release notes** for 3.5.14 → 3.5.15+ or 3.6.x compatibility.
2. **Run local test suite** after each upgrade step:
   ```bash
   ./gradlew clean test
   ```
3. **Verify Docker build** after all changes:
   ```bash
   docker build -f Dockerfile -t ancientdata-test .
   ```
4. **Review breaking changes** in Spring Framework 6.1.14+ (if upgraded).

---

## Expected Outcomes

**After completion:**
- ✅ All Critical and High CVEs resolved.
- ✅ No new security alerts from Dependabot.
- ✅ All local tests pass.
- ✅ Docker image builds successfully.
- ✅ Single commit: `chore: upgrade vulnerable dependencies to resolve Dependabot CVEs`.

---

## Integration with CI/CD

After successful local verification:
1. Push changes to a new branch: `chore/security-upgrades`.
2. Backend CI pipeline will run automatically (test, build, Docker).
3. Merge to `main` once all checks pass.
4. Docker image will auto-publish to Docker Hub.

---

## Notes

- **No breaking changes expected** if upgrading within minor versions (e.g., 3.5.14 → 3.5.15).
- **If upgrading Spring Boot major versions** (e.g., 3.5 → 4.0), review compatibility carefully.
- **Frontend repo is unaffected** — these are backend-only dependencies.
- **Save this prompt** and the resulting `build.gradle` for future Dependabot alerts.

---

## Next Steps

1. Analyze Spring Boot 3.5.14's included transitive versions.
2. Determine if Approach A (BOM upgrade) or Approach B (manual override) is needed.
3. Apply upgrades in order of severity: Critical → High → Moderate → Low.
4. Test locally.
5. Commit and push to CI/CD.
6. Monitor Dependabot for follow-up alerts.

---

**Ready to start the upgrade process?**

Use this prompt in a new conversation. Provide the AI with:
- Full `build.gradle` content (already included above).
- Any local test failures encountered.
- Spring Boot release notes or compatibility concerns.

---

