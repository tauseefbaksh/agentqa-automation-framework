package com.agentqa.agent;

import com.agentqa.spec.ApiSpecDiff;
import com.agentqa.spec.UiSnapshotDiff;

import java.util.Collection;
import java.util.List;

/** Selects only tests that reference changed API fields, endpoints, or UI selectors. */
public interface Planner {
    List<String> impactedTestCaseIds(ApiSpecDiff diff, UiSnapshotDiff uiDiff,
                                     Collection<TestCaseReference> tests);
}
