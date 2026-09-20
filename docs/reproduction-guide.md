# Phase 2 reproduction guide

Use Java 17 and Maven. From the repository root, run:

```bash
./evaluate.sh
```

The default deterministic mode replays `fixtures/llm-responses/` and writes
`results/metrics.json`. It requires no API key and should produce byte-identical
metrics when repeated from a clean checkout.

Live mode runs the complete evaluation three times and writes metric means and
population standard deviations:

```bash
cd core-engine
mvn -pl agentqa-eval exec:java \
  -Dexec.mainClass=com.agentqa.eval.EvalCli \
  -Dexec.args="--live --append-changelog"
```

Live mode requires `ANTHROPIC_API_KEY`; `AGENTQA_LLM_ENDPOINT` and
`AGENTQA_LLM_MODEL` may override the endpoint and model. No credentials are
stored in the repository.

Healing Success Rate (%) = (Verified & Passing Patches / Total Breaking Changes Injected) * 100

False Patch Rate (%) = (Patches Passing the Target Test but Failing Sibling/Regression Tests / Total Proposed Patches) * 100

Time-to-Repair (TTR) Delta (Seconds) = Baseline Manual/One-shot TTR - Agentic Pipeline TTR
