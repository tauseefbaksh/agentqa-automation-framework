package com.agentqa.agent;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Complete outcome of target, sibling, and optional adversarial verification. */
public record VerificationResult(boolean verified, RiskFlag riskFlag,
                                 List<TestExecutionResult> targetAndSiblingResults,
                                 List<TestExecutionResult> adversarialResults,
                                 Path patchedSourceRoot) {
    public VerificationResult {
        Objects.requireNonNull(riskFlag, "riskFlag");
        targetAndSiblingResults = List.copyOf(Objects.requireNonNull(targetAndSiblingResults,
                "targetAndSiblingResults"));
        adversarialResults = List.copyOf(Objects.requireNonNull(adversarialResults,
                "adversarialResults"));
        Objects.requireNonNull(patchedSourceRoot, "patchedSourceRoot");
    }
}
