package com.agentqa.eval;

import java.util.Objects;

/** One breaking-change evaluation record, suitable for deterministic replay. */
public record EvaluationCase(String promptId, String promptVersion, String caseId,
                             boolean breakingChange,
                             boolean verifiedAndPassing, boolean targetPasses,
                             boolean siblingRegressionFails, boolean proposed,
                             double baselineTtrSeconds, double agenticTtrSeconds,
                             String riskFlag) {
    public EvaluationCase {
        Objects.requireNonNull(promptId, "promptId");
        Objects.requireNonNull(promptVersion, "promptVersion");
        Objects.requireNonNull(caseId, "caseId");
        Objects.requireNonNull(riskFlag, "riskFlag");
        if (baselineTtrSeconds < 0 || agenticTtrSeconds < 0) {
            throw new IllegalArgumentException("TTR values must not be negative");
        }
    }

    public EvaluationCase(String promptId, String promptVersion, String caseId,
                           boolean verifiedAndPassing, boolean targetPasses,
                           boolean siblingRegressionFails, boolean proposed,
                           double baselineTtrSeconds, double agenticTtrSeconds,
                           String riskFlag) {
        this(promptId, promptVersion, caseId, true, verifiedAndPassing, targetPasses,
                siblingRegressionFails, proposed, baselineTtrSeconds, agenticTtrSeconds, riskFlag);
    }
}
