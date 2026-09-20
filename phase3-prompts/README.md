# How to use these files

Each stepN-*.txt is the exact instruction text for that roadmap step —
nothing else. Attach files as CLI arguments, not inside the txt.

Run from the agentqa/ repo root:

    aider -f phase3-prompts/stepN-name.txt [files to attach]

## Step routing (unchanged from the roadmap — repeated here for convenience)

| File | Tool recommendation | Files to attach when running |
|---|---|---|
| step1-discovery-server.txt | Aider/Qwen — safe | none |
| step2-config-server.txt | Aider/Qwen — safe | none |
| step3-event-publisher.txt | Copilot preferred; Qwen risky (foundational, cascades) | core-engine/agentqa-agent-core/src/main/java/**/SelfHealer*.java |
| step4-orchestrator-lite.txt | Copilot preferred; Qwen risky (biggest, most concerns at once) | EventPublisher files from step 3, results/metrics.json |
| step5-orchestrator-full.txt | Copilot/Junie preferred | the whole test-orchestrator-service module, services/discovery-server, services/config-server |
| step6-healing-worker.txt | Copilot/Junie preferred | test-orchestrator-service controllers, agentqa-agent-core |
| step7-gateway-skeleton.txt | Aider/Qwen — safe | none |
| step8-gateway-integration.txt | Copilot/Junie preferred (async Kafka + Socket.IO + JWT is finicky) | step 7 skeleton, the Java event payload shape from step 3/4 |
| step9-docker-compose.txt | Aider/Qwen — safe | none |

"Risky" doesn't mean "don't" — if Copilot quota is out, run it through Qwen
anyway with num_ctx raised, but review every diff line by line before
letting aider commit, and re-run the acceptance check for that service
immediately rather than moving on to the next step.

For step 4 specifically: pipe your actual results/metrics.json into the
attach list (not a description of it) so Qwen sees the real field names
(caseId, riskFlag, verifiedAndPassing, etc.) instead of guessing them.
