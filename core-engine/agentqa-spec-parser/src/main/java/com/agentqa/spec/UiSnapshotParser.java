package com.agentqa.spec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Compares serialized component-tree snapshots by stable JSON location. */
public final class UiSnapshotParser {
    private final ObjectMapper objectMapper;

    public UiSnapshotParser() {
        this(new ObjectMapper());
    }

    public UiSnapshotParser(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public UiSnapshotDiff diff(Path oldSnapshot, Path newSnapshot) {
        try {
            JsonNode oldTree = objectMapper.readTree(Objects.requireNonNull(oldSnapshot).toFile());
            JsonNode newTree = objectMapper.readTree(Objects.requireNonNull(newSnapshot).toFile());
            List<UiSnapshotDiff.SelectorChange> changed = new ArrayList<>();
            List<UiSnapshotDiff.SelectorChange> added = new ArrayList<>();
            List<UiSnapshotDiff.SelectorChange> removed = new ArrayList<>();
            compare(oldTree, newTree, "$", changed, added, removed);
            return new UiSnapshotDiff(changed, added, removed);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read UI snapshots", exception);
        }
    }

    private static void compare(JsonNode oldNode, JsonNode newNode, String path,
                                List<UiSnapshotDiff.SelectorChange> changed,
                                List<UiSnapshotDiff.SelectorChange> added,
                                List<UiSnapshotDiff.SelectorChange> removed) {
        String oldSelector = selector(oldNode);
        String newSelector = selector(newNode);
        if (oldSelector != null && newSelector != null && !oldSelector.equals(newSelector)) {
            changed.add(new UiSnapshotDiff.SelectorChange(path, oldSelector, newSelector));
        } else if (oldSelector == null && newSelector != null) {
            added.add(new UiSnapshotDiff.SelectorChange(path, null, newSelector));
        } else if (oldSelector != null) {
            removed.add(new UiSnapshotDiff.SelectorChange(path, oldSelector, null));
        }
        if (oldNode == null || newNode == null || !oldNode.isObject() || !newNode.isObject()) return;
        oldNode.fieldNames().forEachRemaining(name -> {
            if (!newNode.has(name)) compare(oldNode.get(name), null, path + "." + name, changed, added, removed);
            else compare(oldNode.get(name), newNode.get(name), path + "." + name, changed, added, removed);
        });
        newNode.fieldNames().forEachRemaining(name -> {
            if (!oldNode.has(name)) compare(null, newNode.get(name), path + "." + name, changed, added, removed);
        });
    }

    private static String selector(JsonNode node) {
        if (node == null || !node.isObject()) return null;
        for (String name : List.of("data-testid", "dataTestId", "selector")) {
            JsonNode value = node.get(name);
            if (value != null && value.isTextual()) return value.textValue();
        }
        return null;
    }
}
