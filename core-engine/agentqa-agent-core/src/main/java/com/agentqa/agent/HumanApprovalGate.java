package com.agentqa.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** CLI approval gate that writes a verified patch only after explicit approval. */
public final class HumanApprovalGate {
    private final InputStream input;
    private final PrintStream output;

    public HumanApprovalGate() {
        this(System.in, System.out);
    }

    public HumanApprovalGate(InputStream input, PrintStream output) {
        this.input = Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
    }

    /**
     * Requests approval and writes the verified temporary tree to the target
     * only when approved. High-risk proposals require two independent y
     * confirmations.
     */
    public boolean approveAndWrite(HealingProposal proposal, VerificationResult verification,
                                   Path verifiedSourceRoot, Path targetSourceRoot)
            throws IOException {
        Objects.requireNonNull(proposal, "proposal");
        Objects.requireNonNull(verification, "verification");
        Objects.requireNonNull(verifiedSourceRoot, "verifiedSourceRoot");
        Objects.requireNonNull(targetSourceRoot, "targetSourceRoot");
        if (!verification.verified()) {
            output.println("Patch rejected: verification did not pass.");
            return false;
        }
        if (verification.riskFlag() == RiskFlag.HIGH_RISK_RELAXATION) {
            output.println("HIGH_RISK_RELAXATION: this assertion change requires escalation.");
            output.println("Type 'y' to confirm review, then type 'y' again to approve write-back:");
            if (!yes() || !yes()) {
                output.println("Patch not approved.");
                return false;
            }
        } else {
            output.println("Verified patch " + proposal.patchType() + ". Approve write-back? [y/N]");
            if (!yes()) {
                output.println("Patch not approved.");
                return false;
            }
        }
        copyTree(verifiedSourceRoot, targetSourceRoot);
        output.println("Patch approved and written.");
        return true;
    }

    private boolean yes() throws IOException {
        int value;
        do {
            value = input.read();
            if (value == -1) return false;
        } while (Character.isWhitespace(value));
        return Character.toLowerCase(value) == 'y';
    }

    private static void copyTree(Path source, Path target) throws IOException {
        Files.createDirectories(target);
        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path destination = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
