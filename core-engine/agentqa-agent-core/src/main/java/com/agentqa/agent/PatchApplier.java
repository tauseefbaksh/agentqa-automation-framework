package com.agentqa.agent;

import java.io.IOException;
import java.nio.file.Path;

/** Applies a proposal to a copied test source tree. */
@FunctionalInterface
public interface PatchApplier {
    void apply(HealingProposal proposal, Path sourceRoot) throws IOException;
}
