package com.agentqa.runner;

import com.agentqa.agent.TestExecutionResult;
import com.agentqa.agent.TestExecutionService;

import java.nio.file.Path;
import java.util.List;

/** Runs the exact test IDs selected by the agent executor. */
@FunctionalInterface
public interface TestRunner extends TestExecutionService {
    List<TestExecutionResult> run(List<String> testCaseIds, Path sourceRoot);
}
