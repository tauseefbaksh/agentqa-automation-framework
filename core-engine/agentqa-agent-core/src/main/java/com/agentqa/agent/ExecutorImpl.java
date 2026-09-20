package com.agentqa.agent;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Delegates exactly the planner-selected test IDs to the test-runner module. */
public final class ExecutorImpl {
    private final TestExecutionService executionService;

    public ExecutorImpl(TestExecutionService executionService) {
        this.executionService = Objects.requireNonNull(executionService, "executionService");
    }

    public List<TestExecutionResult> execute(List<String> impactedTestCaseIds,
                                             Path sourceRoot) {
        Objects.requireNonNull(impactedTestCaseIds, "impactedTestCaseIds");
        Objects.requireNonNull(sourceRoot, "sourceRoot");
        if (impactedTestCaseIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Test case IDs must not contain null values");
        }
        return List.copyOf(executionService.run(List.copyOf(impactedTestCaseIds), sourceRoot));
    }
}
