package com.agentqa.agent;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HumanApprovalGateTest {
    @Test
    void highRiskRequiresSecondConfirmationBeforeWriteBack() throws Exception {
        Path verified = Files.createTempDirectory("verified");
        Path target = Files.createTempDirectory("target");
        Files.writeString(verified.resolve("Test.java"), "patched");
        VerificationResult result = new VerificationResult(true, RiskFlag.HIGH_RISK_RELAXATION,
                List.of(new TestExecutionResult("target", true, "ok")), List.of(), verified);
        HealingProposal proposal = new HealingProposal("assertion patch", PatchType.ASSERTION_CHANGE);

        boolean approved = new HumanApprovalGate(
                new ByteArrayInputStream("y\nn\n".getBytes()), new PrintStream(new ByteArrayOutputStream()))
                .approveAndWrite(proposal, result, verified, target);

        assertFalse(approved);
        assertFalse(Files.exists(target.resolve("Test.java")));
    }

    @Test
    void highRiskWritesOnlyAfterTwoConfirmations() throws Exception {
        Path verified = Files.createTempDirectory("verified");
        Path target = Files.createTempDirectory("target");
        Files.writeString(verified.resolve("Test.java"), "patched");
        VerificationResult result = new VerificationResult(true, RiskFlag.HIGH_RISK_RELAXATION,
                List.of(new TestExecutionResult("target", true, "ok")), List.of(), verified);
        HealingProposal proposal = new HealingProposal("assertion patch", PatchType.ASSERTION_CHANGE);

        boolean approved = new HumanApprovalGate(
                new ByteArrayInputStream("y\ny\n".getBytes()), new PrintStream(new ByteArrayOutputStream()))
                .approveAndWrite(proposal, result, verified, target);

        assertTrue(approved);
        assertTrue(Files.exists(target.resolve("Test.java")));
    }
}
