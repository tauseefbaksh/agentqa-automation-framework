# AgentQA — Master Architectural Blueprint & Execution Roadmap

**Project**: Autonomous API & UI Test Generation & Self-Healing Framework
**Core engine language**: Java 17+ (non-negotiable across every phase)
**Strategy**: one monorepo, four sequential deliverables, zero duplicate codebases

> **Two honesty notes before the technical content, since they affect how each phase should actually be presented:**
> 1. For Wingify, don't pitch Phase 1 as a finished product — pitch it as "architecture-first, Milestone 0 of a larger system, here's the roadmap." That's both more defensible if a reviewer asks pointed questions and, frankly, more impressive than a repo that quietly implies more than it delivers — reviewers who've hired SDETs before can tell the difference fast.
> 2. Reusing one codebase across CSF302, CSF402, INT361, and CSE451 is architecturally sound (each course genuinely exercises a different layer), but "can I submit overlapping work across four courses" is a policy question your institution answers, not me. Worth a two-line disclosure to each instructor before mid-semester — cheap insurance against an academic-integrity conversation later.

---

## 0. Non-negotiable constraints

- Core test-execution/agent-orchestration logic: **Java 17+, Maven**. No Python anywhere in AgentQA's own logic, evaluation harness, or baseline.
- CSF402 gateway: **Node.js/Express**, no exceptions.
- CSE451 console: **Kotlin + Jetpack Compose**, no Flutter/React Native.
- INT361's syllabus separately requires standalone Python exercises (Unit I basics, general scripting practicals) — those are graded independently of AgentQA and don't touch the shared repo. Keep them in a `coursework/int361-labs/` folder if you need a paper trail, but never import them into `core-engine`.
- Every phase must build on the *same* Maven module boundaries decided below — restructuring later phases is what breaks the "no duplicate codebase" premise.
- From Phase 2 onward, every runnable component ships two execution profiles, not one: **Mode A (local-lite)** and **Mode B (full-stack)** — see Section 2. This is a reproducibility requirement, not an optional nicety.

---

## 1. Repository topology (fixed from day one)

```
agentqa/
├── core-engine/                        # Java, Maven multi-module reactor
│   ├── pom.xml                         # parent: com.agentqa:agentqa-core-parent
│   ├── agentqa-spec-parser/            # OpenAPI + UI snapshot ingestion
│   ├── agentqa-agent-core/             # Planner / Executor / SelfHealer / Memory
│   ├── agentqa-test-runner/            # REST Assured + Playwright-Java execution
│   ├── agentqa-baseline/               # naive one-shot baseline (Phase 2)
│   ├── agentqa-eval/                   # metrics + changelog generator (Phase 2)
│   └── agentqa-reporting/              # HTML/JSON result reports
├── services/                           # Phase 3 — CSF302
│   ├── discovery-server/               # Eureka, port 8761
│   ├── config-server/                  # Spring Cloud Config, port 8888
│   ├── test-orchestrator-service/      # REST API + Postgres/H2, port 8081 — profiles: lite | full
│   └── healing-worker-service/         # Kafka/in-memory consumer, port 8082 — profiles: lite | full
├── realtime-gateway/                   # Phase 3 — CSF402, Node/Express, port 4000
├── devops/                             # Phase 4 — INT361
│   ├── docker-compose.yml              # Mode B — full-stack topology
│   ├── Jenkinsfile
│   ├── monitoring/                     # prometheus.yml, grafana dashboards, cloudwatch alarms
│   └── aws/                            # documented EC2 / RDS / S3 topology
├── mobile-console/                     # Phase 4 — CSE451, Kotlin/Compose
├── fixtures/                           # synthetic OpenAPI specs + UI snapshots, shared v1→v2 pairs
│   ├── llm-responses/                  # NEW — versioned LLM completion cache for --deterministic replay
│   └── trajectories/                   # per-run agent trajectory logs
└── docs/
    ├── README.md
    ├── CHANGELOG.md                    # Improvement Changelog (hackathon)
    ├── reproduction-guide.md           # Mode A/B instructions + formal metric definitions
    └── llm-prompts.md                  # NEW — versioned, frozen Planner/Healer/Baseline prompts
```

`services/` and `realtime-gateway/` depend on `core-engine` artifacts as JAR dependencies (via `mvn install` into local `.m2`) — the agentic loop itself is never rewritten, only wrapped.

---

## 2. Reproducibility architecture: Mode A (local-lite) vs Mode B (full-stack)

Reproducibility is 15% of the hackathon rubric on its own, and it's the thing that quietly wrecks a demo when a judge or grader can't get your stack running in the five minutes they've allotted you. From Phase 2 onward, every runnable component in AgentQA supports two profiles side by side, sharing the same `agentqa-agent-core` logic and diverging only in infrastructure adapters.

**Mode A — local-lite** (`-Dspring.profiles.active=lite` on Spring modules; `--demo-mode --use-fixtures` on the core-engine CLI scripts):
- In-memory **H2** in place of PostgreSQL — schema auto-generated from the same JPA entities, no separate DDL to maintain.
- A single in-process **`EventPublisher`** abstraction in place of Kafka. In lite mode, `test-orchestrator-service` and `healing-worker-service` run as one combined JVM process, so there's no broker to stand up at all.
- A **mock JWT decoder** (`agentqa.demo-mode=true`) that accepts a fixed demo bearer token in place of a live Keycloak realm.
- LLM calls resolved by **replaying `fixtures/llm-responses/`** instead of hitting a live API — no `ANTHROPIC_API_KEY` required to run this mode at all.
- **Target: under 5 minutes wall-clock on a standard developer laptop, zero external Docker dependencies.** This is the mode a hackathon judge or a five-minute grader runs first.

**Mode B — full-stack** (`-Dspring.profiles.active=full`; `devops/docker-compose.yml`):
- The complete production-shaped topology: Keycloak, Kafka, PostgreSQL, Zipkin, Prometheus, Grafana, the Node.js gateway, and all four Spring services, wired exactly as CSF302/CSF402/INT361 require.
- This is the mode that actually earns the course credit in Phase 3/4 — Mode A is a fast sanity check, never a substitute for the graded topology.

Both modes exercise identical `agentqa-agent-core` code; only the `EventPublisher`, JWT decoder, datasource, and LLM client are swapped by profile — never the Planner/Executor/SelfHealer logic itself. `docs/reproduction-guide.md` documents exact commands for both and states plainly which one satisfies which course's grading requirement.

---

## 3. Phase 1 — Decoy / Wingify screening scaffold

**Deadline: tomorrow.**

### 3.1 Overview
This is Milestone 0: a Maven multi-module reactor with the *final* module boundaries already in place, populated with interfaces, Javadoc describing intended behavior, and stub implementations that compile and run (even if they just throw `UnsupportedOperationException` or return canned fixtures). A reviewer skimming the repo should see: correct Maven conventions, the right testing dependencies already wired (REST Assured, Playwright, JUnit 5), a README that reads like a real engineering doc (problem statement, architecture diagram, roadmap), and a CI workflow that actually goes green on push. Nothing here should claim functionality that doesn't exist yet — the roadmap section in the README is where you say what's coming, including a one-line forward note that reproducibility will follow the Mode A / Mode B dual-profile design detailed from Phase 2 onward.

### 3.2 Phase 1 Generation Prompt

```
You are scaffolding the initial commit of a Java project called AgentQA
(Autonomous API & UI Test Generation & Self-Healing Framework). This is
Milestone 0 — an architectural skeleton, not a working product. Do not
implement real logic; every method should compile, run, and either return
a clearly-labeled canned fixture or throw UnsupportedOperationException
with a message pointing to the future implementation.

HARD CONSTRAINTS:
- Java 17, Maven multi-module reactor. No Python, no Kotlin, no Node in
  this phase.
- Root pom.xml: groupId com.agentqa, artifactId agentqa-core-parent,
  packaging pom, modules listed below.

MODULES TO CREATE (all under core-engine/, each its own pom.xml inheriting
the parent):
1. agentqa-spec-parser — interface OpenApiSpecParser (method:
   ApiSpecDiff diff(Path oldSpec, Path newSpec)) and interface
   UiSnapshotParser (method: UiSnapshotDiff diff(Path oldSnapshot,
   Path newSnapshot)). Stub both to throw UnsupportedOperationException.
2. agentqa-agent-core — interfaces Planner, Executor, SelfHealer, and
   HealingMemory, each with one method stub and full Javadoc explaining
   what each will do in Phase 2 (Planner decides which tests are
   impacted by a diff; Executor runs the affected suite via
   agentqa-test-runner; SelfHealer proposes a patch for a failing
   assertion or selector and classifies its own risk level; HealingMemory
   persists past patches to avoid repeat mistakes).
3. agentqa-test-runner — dependencies: io.rest-assured:rest-assured,
   com.microsoft.playwright:playwright, org.junit.jupiter:junit-jupiter.
   Include one real, passing JUnit 5 test that hits a public sandbox API
   (e.g. https://reqres.in or httpbin.org) with REST Assured, so `mvn
   test` is genuinely green, not just compiling.
4. agentqa-baseline, agentqa-eval, agentqa-reporting — empty modules
   with just a pom.xml and a placeholder class, reserved for Phase 2.

ROOT-LEVEL FILES:
- README.md: title, one-paragraph problem statement (test suites break
  silently when APIs/UI change; SDETs spend hours triaging false
  failures instead of writing new coverage), an ASCII or Mermaid
  architecture diagram of the module layout above, a "Current status"
  line stating this is an architectural scaffold under active
  development, and a four-row roadmap table (Milestone 0: scaffold →
  Milestone 1: agentic loop → Milestone 2: microservices split →
  Milestone 3: devops + mobile). Add one forward-looking line noting
  that reproducibility will follow a Mode A (local-lite) / Mode B
  (full-stack) dual-profile design starting Milestone 1.
- .github/workflows/ci.yml — GitHub Actions workflow: on push/PR, set up
  JDK 17 (actions/setup-java), run `mvn -B -q compile`, run `mvn -B test`.
  It must pass on a fresh clone.
- .gitignore (standard Maven/IDE ignores), LICENSE (MIT).
- docs/llm-prompts.md — placeholder file, header only: "Versioned prompt
  registry — populated in Phase 2. Every Planner/Healer/Baseline prompt
  will be frozen here under a stable (id, version) pair."
- fixtures/.gitkeep and fixtures/llm-responses/.gitkeep — reserve the
  directory Phase 2's deterministic-replay cache will populate, so
  Phase 2 never needs to restructure fixtures/.

ACCEPTANCE CRITERIA:
- `mvn clean install` succeeds from a clean checkout with zero manual
  steps.
- `mvn test` produces at least one real passing test.
- No module claims functionality it doesn't have — stubs throw or
  return fixtures, and Javadoc says so explicitly.
- docs/llm-prompts.md and fixtures/llm-responses/ exist as placeholders
  so Phase 2 can populate them without touching directory structure.
```

---

## 4. Phase 2 — Core agentic engine (micro1 Agentic Workflows Hackathon)

**Deadline: Aug 31 – Sep 3.**

### 4.1 Overview mapped to the 100-point rubric

| Criterion | Wt | How AgentQA satisfies it |
|---|---|---|
| Problem & user value | 15% | User = SDETs/QA engineers maintaining API + UI regression suites. Bottleneck = every schema or DOM change silently breaks selectors/assertions; someone manually triages before writing new coverage. |
| Agent solution & engineering | 30% | **Planner** reads a structured spec/DOM diff and decides which tests are impacted → **Executor** runs the affected subset via REST Assured/Playwright → **SelfHealer** proposes a patch and classifies it (selector/payload fix vs. assertion change) → **verification** re-runs the patched test in isolation, plus an adversarial suite for any assertion change → **memory** persists prior patch patterns so recurring breakages heal faster → **human-in-the-loop approval gate**, with mandatory escalation for anything flagged high-risk, before anything merges. That's context, verification, memory, and orchestration all doing real work, not decoration. |
| End-to-end quality | 20% | One full realistic run: ingest a spec, generate a suite, inject a controlled breaking change, watch the agent detect → diagnose → propose → verify → (human approves) → green again, with a final report a QA lead could actually read. |
| Measured improvement | 15% | Fair baseline = single one-shot LLM prompt handed the failing stack trace and the new spec, asked to fix the test in one pass, no verification, no memory. Compare healing accuracy, false-patch rate, and time-to-repair against the full agent, using the formal metric definitions below. |
| Reproducibility | 15% | Fixed synthetic fixtures (`fixtures/spec-v1` → `fixtures/spec-v2`, `fixtures/ui-v1` → `fixtures/ui-v2`), pinned JDK/Maven versions, a `--deterministic` mode that replays `fixtures/llm-responses/` for byte-identical metrics with no API key required, and a `--live` mode that runs 3 iterations and reports mean ± standard deviation when real non-determinism is unavoidable. |
| Hot take / insights | 5% | **This is the headline finding, not a footnote**: an unverified self-healing agent will happily "fix" a test by loosening its assertion until it passes — quietly converting a real regression into a false green. AgentQA's answer is the `HIGH_RISK_RELAXATION` classification: any assertion-altering patch runs through an adversarial validation suite and, even if it passes that suite, still routes to mandatory human escalation rather than auto-merging. State this plainly in both the README and the video — it's the actual insight this project produced. |

**Formal metric definitions** (verbatim — these go into `docs/reproduction-guide.md` exactly as written, and into `agentqa-eval`'s code comments so the implementation can't silently drift from the documented definition):

```
Healing Success Rate (%) = (Verified & Passing Patches / Total Breaking Changes Injected) * 100

False Patch Rate (%) = (Patches Passing the Target Test but Failing Sibling/Regression Tests / Total Proposed Patches) * 100

Time-to-Repair (TTR) Delta (Seconds) = Baseline Manual/One-shot TTR - Agentic Pipeline TTR
```

**Ground rules this design satisfies directly**: consequential actions (merging a patch) are gated behind human approval, with an additional mandatory-escalation tier for high-risk relaxations (rule 04–05); all fixtures are synthetic, no real user data (rule 07); no credentials committed (rule 08); every metric in the changelog traces back to an exact command and a result artifact (rule 09); a clean-environment reproduction path is provided in both Mode A and Mode B (rule 10).

**Deliverable mapping**:
1. Complete code + Improvement Changelog → `README.md` (problem/user framing) + `docs/CHANGELOG.md`.
2. Reproduction guide → `docs/reproduction-guide.md` with exact commands, JDK/Maven versions, expected runtime, Mode A/Mode B instructions, and the three formal metric definitions above, copied verbatim.
3. Solution video (≤5 min) — script it as: baseline run (30s) → one full agent run start-to-finish (2 min) → side-by-side metrics (1 min) → changelog walkthrough + the HIGH_RISK_RELAXATION finding as the headline insight + the one experiment you ripped out (1.5 min).
4. Agent trajectories → structured JSON/log per run under `fixtures/trajectories/`, showing each Planner decision, Executor tool call, Healer proposal (including its risk classification), and the human checkpoint.

### 4.2 Phase 2 Generation Prompt

```
You are implementing the real agentic loop for AgentQA, extending the
Phase-1 skeleton in core-engine/. This submission is judged against a
100-point rubric: problem/user value (15), agent engineering (30),
end-to-end quality (20), measured improvement (15), reproducibility
(15), hot take (5). Every design decision below exists to score against
one of those rows — don't add components that don't earn points.

HARD CONSTRAINTS:
- Everything in core-engine/ stays Java 17+. If you need an LLM call,
  use the Anthropic Java SDK (com.anthropic:anthropic-java) or a plain
  java.net.http.HttpClient call to the Messages API. Never shell out to
  a Python script for orchestration, evaluation, or the baseline.
- Reuse the exact module boundaries from Phase 1. Do not rename or
  restructure agentqa-spec-parser, agentqa-agent-core, agentqa-test-runner.
- This phase runs entirely in Mode A terms (no Docker required at all)
  — that's the baseline reproducibility bar for the hackathon, and
  Phase 3 later adds Mode B parity on top of the same logic.

1. agentqa-spec-parser — implement OpenApiSpecParser.diff() to detect
   renamed/removed/added fields and endpoints between two OpenAPI 3.x
   YAML files (use io.swagger.parser.v3 or manual YAML diffing), and
   UiSnapshotParser.diff() to detect changed data-testid / selector
   attributes between two serialized component-tree JSON snapshots.
   Return a structured ApiSpecDiff / UiSnapshotDiff, not raw text.

2. agentqa-agent-core — implement:
   - PlannerImpl: given a diff, returns the list of test case IDs whose
     assertions or selectors reference the changed field/attribute.
   - ExecutorImpl: invokes agentqa-test-runner on exactly that subset,
     captures pass/fail + stack traces.
   - SelfHealerImpl: for each failure, calls the LLM with (a) the failing
     test source, (b) the specific diff entry implicated, (c) up to 3
     similar past patches pulled from HealingMemoryImpl — and asks for a
     minimal patch (updated selector or assertion only, not a rewrite).
     Classify every proposed patch as either SELECTOR_OR_PAYLOAD_FIX
     (changes a selector, endpoint path, or payload mapping) or
     ASSERTION_CHANGE (alters what the test actually checks). This
     classification drives which verification path runs next — it is
     not cosmetic metadata.
   - VerificationStep: applies the proposed patch to a throwaway copy,
     re-runs the single test, and re-runs the 2-3 nearest neighboring
     tests in the same suite as a regression guard before marking the
     proposal "verified." Additionally: if SelfHealerImpl classified the
     patch as ASSERTION_CHANGE, run it through a small, fixed adversarial
     validation suite first — cases specifically designed to fail if the
     assertion's original boundary contract (equality vs. range, exact
     match vs. substring, required vs. optional field) has been weakened
     rather than genuinely fixed. A patch that fails the adversarial
     suite is rejected outright, full stop. A patch that alters an
     assertion and passes the adversarial suite is still flagged
     `riskFlag = HIGH_RISK_RELAXATION` and routed to mandatory human
     escalation — it never uses the normal auto-approve-eligible path,
     even if every other signal looks clean. `riskFlag` is a field on
     HealingProposal/the trajectory record from this phase forward, so
     Phase 3's entity and Phase 4's mobile screen can surface it without
     re-plumbing.
   - HealingMemoryImpl: append-only JSON log (fixtures/healing-memory.json)
     of {oldPattern, newPattern, diffType, riskFlag, outcome} used to seed
     future prompts.
   - A CLI human-approval gate (simple stdin y/n loop for this phase;
     Phase 4 replaces this with the Android app) that must approve before
     a verified patch is written back to the test source file, and that
     visibly distinguishes HIGH_RISK_RELAXATION proposals in its prompt
     text (not just a plain y/n — force a second confirmation keystroke
     for those).

3. agentqa-baseline — implement a single one-shot baseline: same failing
   test + same diff handed to the LLM in one prompt, "fix this test,"
   applied directly with no verification step, no memory, no planner, no
   risk classification. This is the fair comparison point, not a
   strawman — same model, same fixtures, same evaluation harness.

4. agentqa-eval — evaluation harness supporting two execution modes:
   - `--deterministic` (the default, and the mode judges/CI should run):
     replays cached completions from
     fixtures/llm-responses/{promptId}/{promptVersion}/{caseId}.json
     instead of calling the LLM, guaranteeing byte-identical
     results/metrics.json across runs regardless of API key availability
     or network conditions.
   - `--live` (requires ANTHROPIC_API_KEY): calls the real API. Because
     LLM output is non-deterministic, `--live` MUST run the full
     evaluation set 3 times and report every metric as mean ± standard
     deviation, never a single number. Also support `--record`, which
     runs live and writes fresh entries into fixtures/llm-responses/
     (keyed by the prompt's id+version) so the deterministic cache can be
     refreshed deliberately rather than going silently stale.
   Runs both baseline and agent against fixtures/spec-v1→v2 and
   fixtures/ui-v1→v2 (create ~10 synthetic test cases spanning both,
   including at least one adversarial case where the "obvious" fix would
   hide a real behavioral regression, and at least one case specifically
   engineered to tempt the healer into loosening an assertion, to
   exercise the HIGH_RISK_RELAXATION path).
   Compute exactly these three metrics, using these formal definitions
   verbatim in code comments and in docs/reproduction-guide.md:
     Healing Success Rate (%) = (Verified & Passing Patches /
       Total Breaking Changes Injected) * 100
     False Patch Rate (%) = (Patches Passing the Target Test but
       Failing Sibling/Regression Tests / Total Proposed Patches) * 100
     Time-to-Repair (TTR) Delta (seconds) = Baseline Manual/One-shot TTR
       - Agentic Pipeline TTR
   Every run emits results/metrics.json (raw telemetry: per-case
   pass/fail, timings, riskFlag per proposal) and results/metrics.md (a
   copy-pasteable table), and appends one structured entry to
   docs/CHANGELOG.md in this format:
     | Stage | What you tried and why | Evidence | Decision/learning |
   Each CHANGELOG entry must cite the exact command that was run and the
   path to the metrics.json artifact it produced — no row without a
   traceable command + artifact pair. Populate it with the actual
   progression: baseline → added diff-aware context → added verification
   → added memory → added the adversarial regression guard → final
   combined result.

5. fixtures/ — create the synthetic OpenAPI spec pair and UI snapshot
   pair described above, plus fixtures/llm-responses/ populated via
   `--record` on first run, plus a README in fixtures/ explaining exactly
   what changed between v1 and v2, why it's a realistic break, and which
   case is designed to exercise HIGH_RISK_RELAXATION.

6. docs/llm-prompts.md — a versioned registry of every frozen
   system/user prompt template used by PlannerImpl, SelfHealerImpl, and
   the baseline, each under a stable id and semantic version (e.g.
   `healer.system.v1.0`, `baseline.oneshot.v1.0`). Code references
   templates by (id, version) rather than inlining prompt strings, so a
   wording change is a visible version bump, not a silent behavior
   change — and fixtures/llm-responses/ cache entries, keyed by the same
   (id, version) pair, are correctly invalidated when a prompt bumps
   instead of silently replaying answers to a prompt that no longer
   exists.

7. Root-level scripts: run_baseline.sh, run_agent.sh, evaluate.sh —
   each a thin wrapper around `mvn -pl <module> exec:java` with fixed
   arguments (defaulting to --deterministic), so a judge can run all
   three from a clean clone with no manual editing and no API key.

ACCEPTANCE CRITERIA:
- `./evaluate.sh` (deterministic, default) from a clean clone reproduces
  a byte-identical metrics.json on repeated runs.
- `./evaluate.sh --live` runs 3 iterations and reports mean ± standard
  deviation per metric, documented with an expected variance band.
- docs/CHANGELOG.md has one row per real iteration, each citing its exact
  command + metrics.json path, with a kept/revised/removed decision — no
  placeholder rows.
- At least one fixture case produces a HIGH_RISK_RELAXATION proposal,
  and a JUnit test asserts it is never auto-approved even when it passes
  its target test.
- The human-approval gate actually blocks the write-back until a y/n
  (or the extra confirmation for high-risk proposals) is given; verify
  this with a JUnit test that stubs stdin.
- docs/llm-prompts.md versions match the (id, version) pairs referenced
  in code — add a small test that fails the build if they drift apart.
- No API keys or secrets committed; use environment variables and
  document them in reproduction-guide.md.
```

---

## 5. Phase 3 — Microservices split & real-time gateway (CSF302 + CSF402)

**Deadline: mid-semester.**

### 5.1 Overview

**CSF302 (Spring Boot side).** The agentic core from Phase 2 doesn't get rewritten — it gets wrapped. `test-orchestrator-service` depends on `agentqa-agent-core` and `agentqa-test-runner` as library JARs, exposes them over REST, persists `TestRun` / `TestCase` / `HealingProposal` entities via Spring Data JPA, registers with `discovery-server` (Eureka), and pulls its config from `config-server` (Spring Cloud Config, git-backed). A separate `healing-worker-service` consumes `test-run.events`, invokes the healer library asynchronously, and calls back into the orchestrator via OpenFeign — wrapped in a Resilience4j circuit breaker + retry, since LLM calls are the flakiest part of the whole system and that's exactly what Resilience4j exists to protect against. Spring Security + OAuth2 via Keycloak guards the approve/reject endpoints. Actuator + Micrometer/Prometheus + Zipkin + Grafana give the observability story.

Both services carry the Mode A / Mode B split from Section 2: a `lite` Spring profile (H2, an in-process `EventPublisher`, mocked JWT, orchestrator and healing-worker collapsed into one combined process) for a sub-5-minute sanity check with zero Docker dependencies, and a `full` profile (PostgreSQL, Kafka, Keycloak, genuinely separate services registered through Eureka) that is the actual graded topology.

| CO | Satisfied by |
|---|---|
| CO1 (Spring Boot / REST) | test-orchestrator-service REST layer, JPA entities, DTOs |
| CO2 (microservices / discovery / observability principles) | Eureka + Actuator + planning docs |
| CO3 (build REST APIs + microservices with JPA/Docker) | Full orchestrator implementation + Dockerfile + lite/full profile config |
| CO4 (monolith vs microservices) | Written comparison: Phase-1/2 monolith vs Phase-3 split, in README |
| CO5 (security + observability assessment) | Keycloak/OAuth2 + Zipkin/Grafana writeup with screenshots |
| CO6 (full microservices project) | The four-service system end to end (Mode B) |

**CSF402 (Node gateway side).** `realtime-gateway` is a separate consumer group (`realtime-gateway-group`) on the *same* Kafka topics (`test-run.events`, `healing-proposal.events`) the Java services publish to in Mode B — this is the deliberate polyglot-microservices teaching point: two completely different stacks integrating through one event bus, not through a shared database. On each event it (1) persists the raw trajectory/log line to MongoDB (`executionLogs` collection via Mongoose), and (2) re-broadcasts it over Socket.IO on namespace `/live`, room `test-run:{id}`, so anything subscribed to that run — a browser tab, later the Android app — gets pushed updates in real time. REST endpoints (`GET /api/logs/:testRunId`, paginated/sortable/filterable, documented with Swagger) serve historical queries. JWT auth validates the same Keycloak-issued tokens the Spring side uses in Mode B — one identity provider, two stacks.

| CO | Satisfied by |
|---|---|
| CO1 (architecture, event loop, execution model) | Kafka-consumer + Socket.IO event flow, non-blocking throughout |
| CO2 (async programming, callbacks/promises) | kafkajs consumer handlers, Mongoose async writes |
| CO3 (modules, NPM, dependency mgmt) | package.json with express/socket.io/mongoose/kafkajs/jsonwebtoken |
| CO4 (web server, Express, middleware, sessions) | Express app + JWT middleware + error-handling middleware |
| CO5 (RESTful APIs, pagination, Swagger) | `/api/logs/:testRunId` with query params + swagger-jsdoc docs |
| CO6 (Mongo/Mongoose, WebSockets, JWT auth, RBAC) | executionLogs schema + Socket.IO + JWT verification + role check on export endpoint |

### 5.2 Phase 3 Generation Prompt

```
You are splitting AgentQA's core engine (core-engine/, unchanged from
Phase 2) into a distributed system: four Spring Boot services plus one
Node.js gateway, communicating over Kafka and REST — while preserving a
Mode A (local-lite) execution path alongside the Mode B (full-stack)
topology, per Section 2 of the blueprint.

PART A — Spring Boot side (services/), CSF302:

1. discovery-server — plain Eureka server, port 8761, spring-cloud-
   starter-netflix-eureka-server, standard @EnableEurekaServer. Only
   used in the `full` profile; `lite` mode never registers with it.

2. config-server — spring-cloud-config-server, port 8888, git-backed
   repository (a local `config-repo/` folder is fine for coursework),
   serving application-lite.yml and application-full.yml for the other
   services. Only used in the `full` profile.

3. Introduce an `EventPublisher` interface (publish(topic, event),
   subscribe(topic, handler)) in agentqa-agent-core or a small shared
   module, with two implementations:
   - KafkaEventPublisher (full profile) — real Kafka topics
     test-run.events and healing-proposal.events.
   - InMemoryEventPublisher (lite profile) — an in-process queue; since
     there's no broker, orchestrator and healing-worker logic run inside
     ONE combined Spring Boot application in this profile (a
     `@Profile("lite")` configuration that wires both directly, no
     network hop between them).

4. test-orchestrator-service — package com.agentqa.orchestrator, port
   8081. Depends on agentqa-agent-core and agentqa-test-runner (installed
   as local Maven artifacts). Entities: TestRun (id, specVersion, status,
   startedAt, completedAt), TestCase (id, testRunId FK, name, status,
   assertionDiff), HealingProposal (id, testCaseId FK, proposedPatch,
   patchType[SELECTOR_OR_PAYLOAD_FIX/ASSERTION_CHANGE],
   riskFlag[NONE/HIGH_RISK_RELAXATION],
   status[PENDING/APPROVED/REJECTED], createdAt, reviewedBy). Spring Data
   JPA repositories — H2 in `lite` profile, PostgreSQL in `full` profile,
   same entity classes, only the datasource config differs. Controllers:
     POST /api/v1/test-runs
     GET  /api/v1/test-runs/{id}
     GET  /api/v1/test-runs
     GET  /api/v1/healing-proposals?status=PENDING
     POST /api/v1/healing-proposals/{id}/approve
     POST /api/v1/healing-proposals/{id}/reject
   Publishes via EventPublisher on every status change. Registers with
   Eureka only in `full` profile; pulls config from config-server only in
   `full` profile (lite uses local application-lite.yml). Secures the
   approve/reject endpoints with Spring Security + OAuth2 resource-server
   config: against a real Keycloak realm "agentqa" in `full`, or a mock
   JWT decoder that accepts a fixed demo bearer token
   (`agentqa.demo-mode=true`) in `lite`. Exposes Actuator
   health/metrics/prometheus endpoints in both profiles. Structured
   SLF4J logs; wire Micrometer to Prometheus and add Zipkin tracing in
   `full` profile (skip both in `lite` — not worth the setup cost for a
   5-minute sanity check).

5. healing-worker-service — package com.agentqa.healing, port 8082 (full
   profile only — collapsed into the orchestrator process in lite
   profile, per item 3 above). Kafka consumer on test-run.events filtered
   to status=FAILED. On consume, invokes the embedded agentqa-agent-core
   Planner/Executor/SelfHealer to generate a HealingProposal (including
   its patchType and riskFlag), then calls back to
   test-orchestrator-service via an OpenFeign client (@FeignClient
   registered through Eureka) to POST /api/v1/healing-proposals. Wrap
   both the Feign call and the LLM invocation in a Resilience4j
   CircuitBreaker + Retry, with a documented fallback (e.g. mark proposal
   FAILED_TO_GENERATE rather than silently dropping it).

PART B — Node gateway (realtime-gateway/), CSF402:

6. Express app, port 4000, package.json deps: express, socket.io,
   mongoose, kafkajs, jsonwebtoken, swagger-jsdoc, swagger-ui-express.
   Kafka consumer (consumer group "realtime-gateway-group") subscribed
   to test-run.events and healing-proposal.events — this component only
   runs meaningfully against the `full` profile topology (there's no
   Kafka to consume from in `lite` mode; document this explicitly rather
   than pretending the gateway is part of the 5-minute lite check). On
   each message:
     a) persist to MongoDB via a Mongoose model ExecutionLog
        {testRunId, eventType, payload, riskFlag, timestamp}
     b) emit via Socket.IO namespace "/live" to room `test-run:${id}`
   REST endpoints:
     GET /api/logs/:testRunId  — paginated (?page&limit), sortable
       (?sort=timestamp), filterable (?eventType=)
     GET /api/logs/:testRunId/export — requires an "auditor" role claim
   JWT middleware validates tokens issued by the same Keycloak realm
   "agentqa" the Spring side uses (verify against Keycloak's JWKS
   endpoint). Document all endpoints with swagger-jsdoc, serve at
   /api-docs.

PART C — wiring:
- docker-compose (extend the Phase-4 file, or a temporary one here) for
  postgres, mongo, zookeeper+kafka, keycloak — Mode B only.
- A written comparison in README.md: what request flow looked like as
  the Phase-1/2 monolith vs. now, and why the split was worth the added
  operational complexity (or, honestly, where it wasn't) — and a short
  note on what Mode A intentionally sacrifices (no gateway, no tracing,
  no real auth) in exchange for a 5-minute local check.

ACCEPTANCE CRITERIA:
- `mvn spring-boot:run -Dspring-boot.run.profiles=lite` for
  test-orchestrator-service alone brings up a working combined
  orchestrator+healing pipeline in under 5 minutes with zero Docker
  containers, and can complete one full detect→heal→approve cycle
  against fixtures.
- Submitting a test run through test-orchestrator-service in `full`
  profile produces a visible event in realtime-gateway's MongoDB and a
  Socket.IO message a connected client can observe.
- Approving/rejecting a healing proposal is blocked without a valid JWT
  in `full` profile.
- A HIGH_RISK_RELAXATION proposal is rejected by
  POST /api/v1/healing-proposals/{id}/approve unless a second,
  explicit escalation flag is present on the request — normal approval
  alone must not be sufficient.
- Killing test-orchestrator-service mid-request causes healing-worker-
  service's circuit breaker to open and fall back cleanly, not hang or
  crash — demonstrate with a short chaos test (full profile only).
```

---

## 6. Phase 4 — CI/CD wrapper & mobile console (INT361 + CSE451)

**Deadline: end of semester.**

### 6.1 Overview

**INT361 (DevOps).** Two layers, matching the syllabus's own split between local containerization and AWS deployment. Before touching Docker at all, the fastest sanity check is Mode A: `mvn spring-boot:run -Dspring-boot.run.profiles=lite` on the orchestrator, confirming AgentQA's own logic works before debugging infrastructure wiring. The real deliverable is Mode B: `devops/docker-compose.yml` brings up the entire Phase-3 full-profile stack (postgres, mongo, zookeeper+kafka, keycloak, discovery-server, config-server, test-orchestrator-service, healing-worker-service, realtime-gateway, prometheus, grafana) with one command — this is your Unit IV/V deliverable (containerization, orchestration, monitoring). For the AWS-specific unit (III) — which explicitly names EC2, S3, RDS, and CloudWatch, not just generic containers — document (and where feasible, actually stand up) a real topology: one EC2 instance running Jenkins, one EC2 instance running the docker-compose stack, RDS Postgres replacing the local Postgres container in this environment, an S3 bucket archiving agent-trajectory logs and build artifacts, and CloudWatch alarms on orchestrator error rate / latency. `Jenkinsfile` is a declarative pipeline: checkout → build → test → docker build & push → deploy. Keep this to a single EC2 micro instance if cost is a concern — the syllabus cares that you can articulate the topology and show it working, not that it runs 24/7.

| CO | Satisfied by |
|---|---|
| CO1–CO3 (Python basics, DevOps concepts, cloud deployment) | Separate coursework labs (not in agentqa repo) + written DevOps-principles section in README |
| CO4 (containerization/Docker) | docker-compose.yml (Mode B) + per-service Dockerfiles |
| CO5 (orchestration) | Documented Docker Swarm or ECS variant of the compose file as a stretch deliverable |
| CO6 (continuous monitoring/logging) | Prometheus/Grafana dashboards + CloudWatch alarms + screenshots |

**CSE451 (Mobile).** "AgentQA Console" — a single-activity Jetpack Compose app, package `com.agentqa.console`, with a `NavHost` across four routes: `dashboard` (LazyColumn of test-run summary cards, pulled from `GET /api/v1/test-runs` and live-updated via a Socket.IO Android client subscribed to `/live`), `testRun/{id}` (LazyColumn trajectory viewer — Planner decision, Executor call, Healer proposal, each expandable), `healing/{proposalId}` (diff viewer for the proposed patch with Approve/Reject buttons hitting the orchestrator's REST endpoints — this *is* the human-approval gate from Phase 2, now mobile instead of a CLI y/n prompt, and it's where `HIGH_RISK_RELAXATION` proposals get their real-world escalation UI), and `settings` (API base URL, Keycloak login). Retrofit + OkHttp for REST, Material3 theming, POST_NOTIFICATIONS runtime permission for failed-run alerts. The syllabus's Base44 unit gets a genuine home here rather than a token mention: prototype the `healing/{proposalId}` screen in Base44 first, then hand-build the real Compose version, and write up the comparison as your CO6 reflection.

| CO | Satisfied by |
|---|---|
| CO1 (architecture, components) | App architecture write-up, single-activity Compose structure |
| CO2 (Kotlin, OOP, collections, lambdas, scope functions) | ViewModels, data classes for TestRun/HealingProposal, repository layer |
| CO3 (activity lifecycle, intents) | Explicit intent to share a test report (Android share sheet); lifecycle notes for the live-socket connection |
| CO4 (Compose fundamentals, state) | The four Compose screens, state hoisting for live updates and risk-flag styling |
| CO5 (navigation, lists, Material Design) | NavHost/NavController, LazyColumn, Material3 |
| CO6 (permissions, debugging, testing, Base44) | POST_NOTIFICATIONS handling, Logcat-driven debugging notes, Compose UI tests, Base44 prototype + reflection |

### 6.2 Phase 4 Generation Prompt

```
You are completing AgentQA's DevOps lifecycle and building its mobile
monitoring console, on top of the Phase-3 microservices system
(unchanged).

PART A — DevOps (devops/), INT361:

0. Before wiring the full stack, verify Mode A works in isolation:
   `mvn spring-boot:run -Dspring-boot.run.profiles=lite` from
   test-orchestrator-service should complete one detect→heal→approve
   cycle in under 5 minutes with no Docker running. This isolates
   whether a later failure is in AgentQA's own code or in the infra
   wiring below. Document this as the first step in
   reproduction-guide.md, before the docker-compose instructions.

1. docker-compose.yml at devops/ root (Mode B), services: postgres,
   mongo, zookeeper, kafka, keycloak, discovery-server, config-server,
   test-orchestrator-service, healing-worker-service, realtime-gateway,
   prometheus, grafana. Each service is started with
   SPRING_PROFILES_ACTIVE=full (Java) or the equivalent Node env.
   Each Java/Node service gets its own Dockerfile (multi-stage build:
   Maven/Node build stage → slim JRE/Node runtime stage). Healthchecks
   on every service; depends_on with condition: service_healthy where it
   matters (e.g. orchestrator waiting on postgres).

2. Jenkinsfile — declarative pipeline, stages: Checkout, Build
   (`mvn -B -pl core-engine,services -am package`, then
   `npm ci && npm run build` inside realtime-gateway/), Test
   (`mvn test` across all Java modules — this should include the lite
   profile smoke test from step 0 — `npm test` in the gateway), Docker
   Build & Push (tag with the Jenkins BUILD_NUMBER), Deploy (SSH to the
   target EC2 instance, `docker compose pull && docker compose up -d`).
   Parameterize registry/host via Jenkins credentials, never hardcode.

3. devops/aws/ — a README documenting the real topology: one EC2
   instance for Jenkins, one EC2 instance for the deployed stack, an
   RDS Postgres instance (replacing the local postgres container in
   this environment), an S3 bucket for archiving agent-trajectory logs
   and build artifacts, and CloudWatch alarms on
   test-orchestrator-service's error rate and p95 latency. Include the
   exact AWS CLI or console steps taken, and current region/instance-
   type choices, so a grader can verify the setup without you having to
   keep it running indefinitely.

4. devops/monitoring/ — prometheus.yml scraping all Actuator
   /actuator/prometheus endpoints, a Grafana dashboard JSON for
   orchestrator request rate / error rate / latency, and a written note
   on which metric would be the first CloudWatch alarm you'd wire in
   production and why.

PART B — Mobile (mobile-console/), CSE451:

5. New Android Studio project, package com.agentqa.console, single
   Activity hosting Jetpack Compose, minSdk 26. build.gradle.kts deps:
   Compose BOM, Navigation-Compose, Retrofit2 + OkHttp3 + a JSON
   converter, a Socket.IO Android client (io.socket:socket.io-client),
   Material3.

6. NavHost with routes:
   - "dashboard" — DashboardScreen: LazyColumn of TestRunSummary cards
     (id, status badge, timestamp), backed by GET /api/v1/test-runs
     via Retrofit, refreshed on a Socket.IO event from namespace "/live".
   - "testRun/{id}" — TestRunDetailScreen: LazyColumn of trajectory
     steps (Planner decision → Executor call → Healer proposal), each
     row expandable, sourced from GET /api/logs/:testRunId on the
     Node gateway.
   - "healing/{proposalId}" — HealingApprovalScreen: side-by-side diff
     of the current vs. proposed selector/assertion, Approve/Reject
     buttons calling POST /api/v1/healing-proposals/{id}/approve|reject
     on the orchestrator, with a JWT attached from the stored Keycloak
     session. If the proposal's riskFlag is HIGH_RISK_RELAXATION, the
     screen must render a visually distinct warning state (e.g. a red
     banner naming the risk explicitly) and require a second, separate
     confirmation tap before the Approve action fires — this is the
     mandatory human-escalation path from Phase 2, completed here.
   - "settings" — SettingsScreen: editable API base URL, login/logout.

7. Handle POST_NOTIFICATIONS runtime permission (Android 13+) to alert
   on a failed test run or a newly pending healing proposal (with the
   notification text distinguishing a normal pending proposal from a
   HIGH_RISK_RELAXATION one); degrade gracefully (in-app banner only)
   if denied.

8. Testing: JUnit + createComposeRule UI tests for DashboardScreen
   (verify cards render from a fake Retrofit response) and
   HealingApprovalScreen (verify Approve is blocked on a
   HIGH_RISK_RELAXATION proposal until the second confirmation is given,
   and triggers the expected API call afterward with a mocked client).

9. Base44 exercise: build a throwaway prototype of
   HealingApprovalScreen in Base44, then discard it in favor of the
   hand-built Compose version above. Write docs/base44-reflection.md
   comparing iteration speed, the point where Base44's generated UI
   diverged from what you actually needed, and what that implies about
   where AI-assisted mobile prototyping is (and isn't) production-ready.

ACCEPTANCE CRITERIA:
- `docker compose up` from devops/ brings up the full stack from a
  clean clone with no manual steps beyond providing Keycloak realm
  import and environment variables documented in reproduction-guide.md.
- Approving a healing proposal from the Android app is reflected in
  test-orchestrator-service's Postgres row within a few seconds, and
  the dashboard updates without a manual pull-to-refresh.
- A HIGH_RISK_RELAXATION proposal cannot be approved from the app with
  a single tap — the UI test from step 8 is the proof.
- Jenkinsfile runs end-to-end against a local Jenkins instance (a
  screenshot/log of a green pipeline run is sufficient evidence for the
  course deliverable if EC2 isn't kept running long-term).
```

---

## 7. Cross-phase execution checklist

| Phase | Window | Blocking dependency | Definition of done |
|---|---|---|---|
| 1 — Decoy | Tomorrow | None | `mvn clean install` green, CI green, README doesn't overclaim, llm-prompts.md + fixtures/llm-responses/ reserved |
| 2 — Hackathon | Aug 31 – Sep 3 | Phase 1 module boundaries frozen | `./evaluate.sh` (deterministic) byte-identical across runs; `--live` reports mean ± stdev over 3 iterations; a HIGH_RISK_RELAXATION case exists and is provably never auto-approved |
| 3 — Microservices | Mid-semester | Phase 2 agentqa-agent-core stable | Mode A: orchestrator alone completes a full cycle in <5 min, no Docker. Mode B: full event flow orchestrator → Kafka → Node → Mongo → Socket.IO demoed live |
| 4 — DevOps + Mobile | End of semester | Phase 3 services running | Mode A smoke test passes before Mode B docker-compose is even attempted; Android app enforces the two-tap escalation for HIGH_RISK_RELAXATION and approves a real healing proposal end to end |

## 8. Risk register

- **LLM non-determinism in Phase 2 metrics** — mitigated by `--deterministic` fixture replay as the default judged mode; `--live` mode documents an expected variance band (mean ± stdev over 3 runs) rather than claiming a single fixed number.
- **Fixture-cache drift** — `fixtures/llm-responses/` can go stale if a prompt or input changes without re-running `--record`. Key the cache by (promptId, promptVersion, caseId) and hard-fail deterministic runs on a cache-key miss rather than silently falling back to a live call or a mismatched cached answer.
- **Mode A / Mode B behavioral drift** — lite and full profiles are different code paths (EventPublisher, JWT decoder, datasource) even though they share agentqa-agent-core; mitigate with a small contract test suite run against both implementations so they can't silently diverge in behavior over a semester.
- **Over-flagging HIGH_RISK_RELAXATION** — if the adversarial suite is too strict, every assertion change gets escalated and the self-healing story quietly becomes a self-flagging story. Track and report the classifier's own false-positive rate (proposals flagged high-risk that a human reviewer judges were actually fine), not just the false-patch rate of the underlying healer.
- **AWS costs in Phase 4** — don't leave EC2/RDS running between grading windows; spin up, capture evidence (logs, screenshots, CloudWatch console views), spin down.
- **Scope creep across four courses** — the temptation is to gold-plate Phase 3/4 since they're graded separately from the hackathon; timebox each to what its course's CO table above actually requires.
- **Keycloak as a shared dependency** — if it goes down, both the Spring and Node auth paths break simultaneously in `full` profile; Mode A's mocked JWT decoder is the documented fallback for demo days, clearly labeled as non-production.
