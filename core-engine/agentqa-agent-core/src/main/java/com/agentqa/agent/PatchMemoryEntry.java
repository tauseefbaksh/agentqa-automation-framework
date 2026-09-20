package com.agentqa.agent;

import java.util.Objects;

/** One append-only healing-memory event. */
public record PatchMemoryEntry(String oldPattern, String newPattern,
                               String diffType, String riskFlag, String outcome) {
    public PatchMemoryEntry {
        Objects.requireNonNull(oldPattern, "oldPattern");
        Objects.requireNonNull(newPattern, "newPattern");
        Objects.requireNonNull(diffType, "diffType");
        Objects.requireNonNull(riskFlag, "riskFlag");
        Objects.requireNonNull(outcome, "outcome");
    }
}
