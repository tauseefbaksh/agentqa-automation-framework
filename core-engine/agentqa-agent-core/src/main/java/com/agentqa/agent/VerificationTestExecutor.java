package com.agentqa.agent;

import java.nio.file.Path;

/** Executes one test against a throwaway patched source tree. */
@FunctionalInterface
public interface VerificationTestExecutor {
    TestExecutionResult run(String testCaseId, Path sourceRoot) throws Exception;
}
