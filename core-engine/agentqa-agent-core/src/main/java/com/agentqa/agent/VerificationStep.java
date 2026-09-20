package com.agentqa.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Verifies a proposal in a temporary copy before it can be written back.
 *
 * <p>The executor owns the actual test technology (JUnit, Playwright, or REST
 * Assured), keeping this phase independent of a particular runner.</p>
 */
public final class VerificationStep {
    private final PatchApplier patchApplier;
    private final VerificationTestExecutor testExecutor;

    public VerificationStep(PatchApplier patchApplier, VerificationTestExecutor testExecutor) {
        this.patchApplier = Objects.requireNonNull(patchApplier, "patchApplier");
        this.testExecutor = Objects.requireNonNull(testExecutor, "testExecutor");
    }

    public VerificationResult verify(HealingProposal proposal, Path testSourceRoot,
                                     String targetTestId, List<String> siblingTestIds,
                                     List<String> adversarialTestIds) throws Exception {
        Objects.requireNonNull(proposal, "proposal");
        Objects.requireNonNull(testSourceRoot, "testSourceRoot");
        Objects.requireNonNull(targetTestId, "targetTestId");
        Objects.requireNonNull(siblingTestIds, "siblingTestIds");
        Objects.requireNonNull(adversarialTestIds, "adversarialTestIds");
        if (!Files.isDirectory(testSourceRoot)) {
            throw new IOException("Test source root is not a directory: " + testSourceRoot);
        }
        if (proposal.patchType() == PatchType.ASSERTION_CHANGE && adversarialTestIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Assertion changes require at least one adversarial validation test");
        }

        Path temporaryRoot = Files.createTempDirectory("agentqa-verification-");
        copyTree(testSourceRoot, temporaryRoot);
        patchApplier.apply(proposal, temporaryRoot);

        List<TestExecutionResult> normalResults = new ArrayList<>();
        normalResults.add(run(targetTestId, temporaryRoot));
        siblingTestIds.stream().limit(3)
                .filter(id -> !targetTestId.equals(id))
                .map(id -> runUnchecked(id, temporaryRoot))
                .forEach(normalResults::add);

        List<TestExecutionResult> adversarialResults = List.of();
        if (proposal.patchType() == PatchType.ASSERTION_CHANGE) {
            adversarialResults = adversarialTestIds.stream()
                    .map(id -> runUnchecked(id, temporaryRoot))
                    .toList();
        }

        boolean normalPassed = normalResults.stream().allMatch(TestExecutionResult::passed);
        boolean adversarialPassed = adversarialResults.stream().allMatch(TestExecutionResult::passed);
        boolean verified = normalPassed && adversarialPassed;
        RiskFlag riskFlag = proposal.patchType() == PatchType.ASSERTION_CHANGE && adversarialPassed
                ? RiskFlag.HIGH_RISK_RELAXATION : RiskFlag.NONE;
        return new VerificationResult(verified, riskFlag, normalResults,
                adversarialResults, temporaryRoot);
    }

    private TestExecutionResult run(String testId, Path root) throws Exception {
        return testExecutor.run(testId, root);
    }

    private TestExecutionResult runUnchecked(String testId, Path root) {
        try {
            return run(testId, root);
        } catch (Exception exception) {
            return new TestExecutionResult(testId, false, exception.toString());
        }
    }

    private static void copyTree(Path source, Path target) throws IOException {
        try (var paths = Files.walk(source)) {
            paths.forEach(path -> {
                try {
                    Path destination = target.resolve(source.relativize(path));
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException exception) {
                    throw new CopyFailure(exception);
                }
            });
        } catch (CopyFailure exception) {
            throw exception.cause;
        }
    }

    private static final class CopyFailure extends RuntimeException {
        private final IOException cause;

        private CopyFailure(IOException cause) {
            this.cause = cause;
        }
    }
}
