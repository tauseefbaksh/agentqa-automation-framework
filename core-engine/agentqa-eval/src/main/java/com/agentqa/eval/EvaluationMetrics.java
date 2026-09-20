package com.agentqa.eval;

import java.util.List;

/** Exact Phase 2 metrics and per-case telemetry. */
public record EvaluationMetrics(double healingSuccessRatePercent,
                                double falsePatchRatePercent,
                                double ttrDeltaSeconds,
                                List<EvaluationCase> cases) {
    public EvaluationMetrics {
        cases = List.copyOf(cases);
    }

    public static EvaluationMetrics calculate(List<EvaluationCase> cases) {
        long breaking = cases.stream().filter(EvaluationCase::breakingChange).count();
        long verified = cases.stream().filter(EvaluationCase::verifiedAndPassing).count();
        long proposed = cases.stream().filter(EvaluationCase::proposed).count();
        long falsePatches = cases.stream()
                .filter(c -> c.proposed() && c.targetPasses() && c.siblingRegressionFails())
                .count();
        double successRate = percentage(verified, breaking);
        double falsePatchRate = percentage(falsePatches, proposed);
        double ttrDelta = cases.stream()
                .mapToDouble(c -> c.baselineTtrSeconds() - c.agenticTtrSeconds())
                .sum();
        return new EvaluationMetrics(successRate, falsePatchRate, ttrDelta, cases);
    }

    private static double percentage(long numerator, long denominator) {
        return denominator == 0 ? 0.0 : numerator * 100.0 / denominator;
    }
}
