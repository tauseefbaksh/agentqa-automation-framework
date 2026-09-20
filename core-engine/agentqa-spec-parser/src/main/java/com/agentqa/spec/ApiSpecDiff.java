package com.agentqa.spec;

import java.util.List;
import java.util.Objects;

/** Structured changes found between two OpenAPI documents. */
public final class ApiSpecDiff {
    private final List<String> addedEndpoints;
    private final List<String> removedEndpoints;
    private final List<FieldChange> addedFields;
    private final List<FieldChange> removedFields;
    private final List<FieldRename> renamedFields;

    public ApiSpecDiff(List<String> addedEndpoints, List<String> removedEndpoints,
                       List<FieldChange> addedFields, List<FieldChange> removedFields,
                       List<FieldRename> renamedFields) {
        this.addedEndpoints = immutable(addedEndpoints);
        this.removedEndpoints = immutable(removedEndpoints);
        this.addedFields = immutable(addedFields);
        this.removedFields = immutable(removedFields);
        this.renamedFields = immutable(renamedFields);
    }

    public List<String> getAddedEndpoints() { return addedEndpoints; }
    public List<String> getRemovedEndpoints() { return removedEndpoints; }
    public List<FieldChange> getAddedFields() { return addedFields; }
    public List<FieldChange> getRemovedFields() { return removedFields; }
    public List<FieldRename> getRenamedFields() { return renamedFields; }
    public List<String> addedEndpoints() { return addedEndpoints; }
    public List<String> removedEndpoints() { return removedEndpoints; }
    public List<FieldChange> addedFields() { return addedFields; }
    public List<FieldChange> removedFields() { return removedFields; }
    public List<FieldRename> renamedFields() { return renamedFields; }

    public record FieldChange(String schema, String field) {
        public FieldChange {
            Objects.requireNonNull(schema, "schema");
            Objects.requireNonNull(field, "field");
        }
    }

    public record FieldRename(String schema, String oldField, String newField) {
        public FieldRename {
            Objects.requireNonNull(schema, "schema");
            Objects.requireNonNull(oldField, "oldField");
            Objects.requireNonNull(newField, "newField");
        }
    }

    private static <T> List<T> immutable(List<T> values) {
        return List.copyOf(Objects.requireNonNull(values, "values"));
    }
}
