package com.agentqa.agent;

import java.nio.file.Path;
import java.util.List;

/** Execution boundary implemented by the JUnit/REST Assured runner module. */
@FunctionalInterface
public interface TestExecutionService {
    List<TestExecutionResult> run(List<String> testCaseIds, Path sourceRoot);
}
