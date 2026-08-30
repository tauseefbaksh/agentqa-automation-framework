# AgentQA Core

## Executive Summary

AgentQA is a comprehensive platform designed to automate and enhance the quality assurance process. This repository serves as the core engine for the platform, providing the necessary components and infrastructure to support various QA tasks.

## Module Architecture

| Module Name                | Description                                                                 |
|----------------------------|-----------------------------------------------------------------------------|
| agentqa-spec-parser        | Parses and processes API specifications.                                    |
| agentqa-agent-core         | Core logic for the agent, including planning, execution, and healing.       |
| agentqa-test-runner        | Runs tests using REST Assured, Playwright, and JUnit 5.                      |
| agentqa-baseline           | Provides baseline configurations and utilities.                             |
| agentqa-eval               | Evaluates test results and provides insights.                               |
| agentqa-reporting          | Generates reports based on test results.                                      |

## Roadmap

| Milestone | Description                                                                 |
|-----------|----------------------------------------------------------------------------|
| Milestone 1 | Implement core engine components and basic test infrastructure.               |
| Milestone 2 | Integrate advanced AI and machine learning capabilities.                    |
| Milestone 3 | Enhance reporting and analytics features.                                   |

## Note

From Milestone 1 onward, reproducibility will follow a Mode A (local-lite) and Mode B (full-stack) dual-profile design.
