package com.agentqa.eval;

import java.util.List;

/** Three-run live evaluation summary with mean and population standard deviation. */
public record LiveEvaluationSummary(int iterations, double healingSuccessRateMean,
                                    double healingSuccessRateStdDev,
                                    double falsePatchRateMean, double falsePatchRateStdDev,
                                    double ttrDeltaMean, double ttrDeltaStdDev,
                                    List<EvaluationMetrics> runs) {
    public LiveEvaluationSummary {
        runs = List.copyOf(runs);
    }

    public static LiveEvaluationSummary calculate(List<EvaluationMetrics> runs) {
        return new LiveEvaluationSummary(runs.size(),
                mean(runs, Metric.SUCCESS), stdDev(runs, Metric.SUCCESS),
                mean(runs, Metric.FALSE_PATCH), stdDev(runs, Metric.FALSE_PATCH),
                mean(runs, Metric.TTR), stdDev(runs, Metric.TTR), runs);
    }

    private enum Metric { SUCCESS, FALSE_PATCH, TTR }

    private static double mean(List<EvaluationMetrics> runs, Metric metric) {
        return runs.stream().mapToDouble(run -> value(run, metric)).average().orElse(0.0);
    }

    private static double stdDev(List<EvaluationMetrics> runs, Metric metric) {
        double mean = mean(runs, metric);
        return Math.sqrt(runs.stream().mapToDouble(run -> {
            double delta = value(run, metric) - mean;
            return delta * delta;
        }).average().orElse(0.0));
    }

    private static double value(EvaluationMetrics run, Metric metric) {
        return switch (metric) {
            case SUCCESS -> run.healingSuccessRatePercent();
            case FALSE_PATCH -> run.falsePatchRatePercent();
            case TTR -> run.ttrDeltaSeconds();
        };
    }
}
