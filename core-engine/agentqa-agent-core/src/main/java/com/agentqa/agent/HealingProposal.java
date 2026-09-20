package com.agentqa.agent;

import java.util.Objects;

/** A minimal LLM-proposed change and its safety classification. */
public record HealingProposal(String patch, PatchType patchType) {
    public HealingProposal {
        Objects.requireNonNull(patch, "patch");
        Objects.requireNonNull(patchType, "patchType");
        if (patch.isBlank()) {
            throw new IllegalArgumentException("patch must not be blank");
        }
    }
}
