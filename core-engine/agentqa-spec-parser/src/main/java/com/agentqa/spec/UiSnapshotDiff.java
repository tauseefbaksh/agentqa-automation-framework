package com.agentqa.spec;

import java.util.List;
import java.util.Objects;

/** Structured selector changes found between two serialized UI snapshots. */
public final class UiSnapshotDiff {
    private final List<SelectorChange> changedSelectors;
    private final List<SelectorChange> addedSelectors;
    private final List<SelectorChange> removedSelectors;

    public UiSnapshotDiff(List<SelectorChange> changedSelectors,
                          List<SelectorChange> addedSelectors,
                          List<SelectorChange> removedSelectors) {
        this.changedSelectors = List.copyOf(Objects.requireNonNull(changedSelectors, "changedSelectors"));
        this.addedSelectors = List.copyOf(Objects.requireNonNull(addedSelectors, "addedSelectors"));
        this.removedSelectors = List.copyOf(Objects.requireNonNull(removedSelectors, "removedSelectors"));
    }

    public List<SelectorChange> getChangedSelectors() { return changedSelectors; }
    public List<SelectorChange> getAddedSelectors() { return addedSelectors; }
    public List<SelectorChange> getRemovedSelectors() { return removedSelectors; }
    public List<SelectorChange> changedSelectors() { return changedSelectors; }
    public List<SelectorChange> addedSelectors() { return addedSelectors; }
    public List<SelectorChange> removedSelectors() { return removedSelectors; }

    public record SelectorChange(String path, String oldSelector, String newSelector) {
        public SelectorChange {
            Objects.requireNonNull(path, "path");
        }
    }
}
