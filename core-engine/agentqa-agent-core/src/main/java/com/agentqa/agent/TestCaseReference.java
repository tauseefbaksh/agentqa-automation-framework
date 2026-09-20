package com.agentqa.agent;

import java.util.Objects;

/** Test metadata used by the planner to match changed API/UI references. */
public record TestCaseReference(String id, String source) {
    public TestCaseReference {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(source, "source");
    }
}
