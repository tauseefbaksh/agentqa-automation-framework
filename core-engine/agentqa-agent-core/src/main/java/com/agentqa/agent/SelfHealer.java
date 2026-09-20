package com.agentqa.agent;

import com.agentqa.spec.ApiSpecDiff;
import com.agentqa.spec.UiSnapshotDiff;

import java.io.IOException;

/** Produces a minimal, classified patch for one failing test. */
public interface SelfHealer {
    HealingProposal propose(String failingTestSource, String diffDescription) throws IOException, InterruptedException;

    default HealingProposal propose(String failingTestSource, ApiSpecDiff diff)
            throws IOException, InterruptedException {
        return propose(failingTestSource, DiffDescriptions.api(diff));
    }

    default HealingProposal propose(String failingTestSource, UiSnapshotDiff diff)
            throws IOException, InterruptedException {
        return propose(failingTestSource, DiffDescriptions.ui(diff));
    }
}
