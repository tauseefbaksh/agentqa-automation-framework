package com.agentqa.agent;

import java.util.Objects;

/** Result of one verification test invocation. */
public record TestExecutionResult(String testCaseId, boolean passed, String output) {
    public TestExecutionResult {
        Objects.requireNonNull(testCaseId, "testCaseId");
        Objects.requireNonNull(output, "output");
    }
}
