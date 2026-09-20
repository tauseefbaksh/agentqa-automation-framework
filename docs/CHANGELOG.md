# Improvement Changelog

| Stage | What you tried and why | Evidence | Decision/learning |
|---|---|---|---|
| DETERMINISTIC | Evaluated cached AgentQA cases | command: `mvn -pl core-engine/agentqa-eval exec:java -- --deterministic`; artifact: `..\results\metrics.json` | Metrics recorded for the selected evaluation mode. |
| DETERMINISTIC | Evaluated cached AgentQA cases | command: `mvn -pl core-engine/agentqa-eval exec:java -- --deterministic`; artifact: `..\results\metrics.json` | Metrics recorded for the selected evaluation mode. |
| DETERMINISTIC | Evaluated AgentQA cases | command: `./evaluate.sh --deterministic --append-changelog`; artifact: `..\results\metrics.json` | Deterministic replay retained for byte-identical evaluation. |
| DETERMINISTIC | Evaluated AgentQA cases | command: `./evaluate.sh --deterministic --append-changelog`; artifact: `..\results\metrics.json` | Deterministic replay retained for byte-identical evaluation. |
| DETERMINISTIC | Evaluated AgentQA cases | command: `./evaluate.sh --deterministic --append-changelog`; artifact: `..\results\metrics.json` | Deterministic replay retained for byte-identical evaluation. |
