package com.agentqa.agent;

import com.agentqa.spec.ApiSpecDiff;
import com.agentqa.spec.UiSnapshotDiff;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Default deterministic planner based on textual references in test sources. */
public final class PlannerImpl implements Planner {
    @Override
    public List<String> impactedTestCaseIds(ApiSpecDiff diff, UiSnapshotDiff uiDiff,
                                             Collection<TestCaseReference> tests) {
        Objects.requireNonNull(diff, "diff");
        Objects.requireNonNull(uiDiff, "uiDiff");
        Objects.requireNonNull(tests, "tests");
        Set<String> tokens = new LinkedHashSet<>();
        diff.addedEndpoints().forEach(tokens::add);
        diff.removedEndpoints().forEach(tokens::add);
        diff.addedFields().forEach(field -> tokens.add(field.field()));
        diff.removedFields().forEach(field -> tokens.add(field.field()));
        diff.renamedFields().forEach(rename -> {
            tokens.add(rename.oldField());
            tokens.add(rename.newField());
        });
        uiDiff.changedSelectors().forEach(change -> {
            tokens.add(change.oldSelector());
            tokens.add(change.newSelector());
        });
        uiDiff.addedSelectors().forEach(change -> tokens.add(change.newSelector()));
        uiDiff.removedSelectors().forEach(change -> tokens.add(change.oldSelector()));

        return tests.stream()
                .filter(Objects::nonNull)
                .filter(test -> referencesAny(test.source(), tokens))
                .map(TestCaseReference::id)
                .distinct()
                .toList();
    }

    public List<String> impactedTestCaseIds(ApiSpecDiff diff, Collection<TestCaseReference> tests) {
        return impactedTestCaseIds(diff, emptyUiDiff(), tests);
    }

    public List<String> impactedTestCaseIds(UiSnapshotDiff diff, Collection<TestCaseReference> tests) {
        return impactedTestCaseIds(emptyApiDiff(), diff, tests);
    }

    private static boolean referencesAny(String source, Set<String> tokens) {
        String normalized = source.toLowerCase(Locale.ROOT);
        return tokens.stream().filter(Objects::nonNull)
                .map(token -> token.toLowerCase(Locale.ROOT))
                .anyMatch(normalized::contains);
    }

    private static ApiSpecDiff emptyApiDiff() {
        return new ApiSpecDiff(List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static UiSnapshotDiff emptyUiDiff() {
        return new UiSnapshotDiff(List.of(), List.of(), List.of());
    }
}
