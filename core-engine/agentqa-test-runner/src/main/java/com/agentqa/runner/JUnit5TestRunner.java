package com.agentqa.runner;

import com.agentqa.agent.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.nio.file.Path;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * In-process JUnit 5 adapter. IDs use {@code fully.qualified.Class#method}
 * or a fully qualified class name; the source root is added to the launcher
 * classpath by the caller's test build/runtime.
 */
public final class JUnit5TestRunner implements TestRunner {
    @Override
    public List<TestExecutionResult> run(List<String> testCaseIds, Path sourceRoot) {
        Objects.requireNonNull(testCaseIds, "testCaseIds");
        Objects.requireNonNull(sourceRoot, "sourceRoot");
        List<TestExecutionResult> results = new ArrayList<>();
        for (String testCaseId : testCaseIds) {
            results.add(runOne(testCaseId, sourceRoot));
        }
        return results;
    }

    private TestExecutionResult runOne(String testCaseId, Path sourceRoot) {
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
        try {
            URL rootUrl = sourceRoot.toAbsolutePath().toUri().toURL();
            try (URLClassLoader testLoader = new URLClassLoader(
                    new URL[]{rootUrl}, previousLoader)) {
                Thread.currentThread().setContextClassLoader(testLoader);
                return executeOne(testCaseId);
            }
        } catch (Exception exception) {
            return new TestExecutionResult(testCaseId, false, exception.toString());
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
        }
    }

    private TestExecutionResult executeOne(String testCaseId) {
        try {
            LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request();
            if (testCaseId.contains("#")) {
                String[] parts = testCaseId.split("#", 2);
                builder.selectors(DiscoverySelectors.selectMethod(parts[0], parts[1]));
            } else {
                builder.selectors(DiscoverySelectors.selectClass(testCaseId));
            }
            LauncherDiscoveryRequest request = builder.build();
            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            Launcher launcher = LauncherFactory.create();
            launcher.registerTestExecutionListeners(listener);
            launcher.execute(request);
            var summary = listener.getSummary();
            boolean passed = summary.getFailures().isEmpty()
                    && summary.getTestsFoundCount() > 0;
            String output = summary.getFailures().stream()
                    .map(failure -> failure.getTestIdentifier().getDisplayName()
                            + ": " + failure.getException())
                    .reduce("", (left, right) -> left.isEmpty() ? right : left + System.lineSeparator() + right);
            if (output.isEmpty()) {
                output = "testsFound=" + summary.getTestsFoundCount()
                        + ", testsSucceeded=" + summary.getTestsSucceededCount();
            }
            return new TestExecutionResult(testCaseId, passed, output);
        } catch (RuntimeException exception) {
            return new TestExecutionResult(testCaseId, false, exception.toString());
        }
    }
}
