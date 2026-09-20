package com.agentqa.agent;

import com.agentqa.spec.ApiSpecDiff;
import com.agentqa.spec.UiSnapshotDiff;

final class DiffDescriptions {
    private DiffDescriptions() {
    }

    static String api(ApiSpecDiff diff) {
        return "addedEndpoints=" + diff.addedEndpoints()
                + ", removedEndpoints=" + diff.removedEndpoints()
                + ", addedFields=" + diff.addedFields()
                + ", removedFields=" + diff.removedFields()
                + ", renamedFields=" + diff.renamedFields();
    }

    static String ui(UiSnapshotDiff diff) {
        return "changedSelectors=" + diff.changedSelectors()
                + ", addedSelectors=" + diff.addedSelectors()
                + ", removedSelectors=" + diff.removedSelectors();
    }
}
