package com.agentqa.spec;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Compares two OpenAPI 3.x documents without exposing parser implementation details. */
public final class OpenApiSpecParser {
    public ApiSpecDiff diff(Path oldSpec, Path newSpec) {
        Objects.requireNonNull(oldSpec, "oldSpec");
        Objects.requireNonNull(newSpec, "newSpec");
        OpenAPI oldApi = read(oldSpec);
        OpenAPI newApi = read(newSpec);

        Set<String> oldEndpoints = endpointKeys(oldApi);
        Set<String> newEndpoints = endpointKeys(newApi);
        List<String> addedEndpoints = difference(newEndpoints, oldEndpoints);
        List<String> removedEndpoints = difference(oldEndpoints, newEndpoints);

        List<ApiSpecDiff.FieldChange> addedFields = new ArrayList<>();
        List<ApiSpecDiff.FieldChange> removedFields = new ArrayList<>();
        List<ApiSpecDiff.FieldRename> renamedFields = new ArrayList<>();
        Map<String, Schema> oldSchemas = schemas(oldApi);
        Map<String, Schema> newSchemas = schemas(newApi);
        for (String schemaName : union(oldSchemas.keySet(), newSchemas.keySet())) {
            Set<String> oldFields = properties(oldSchemas.get(schemaName));
            Set<String> newFields = properties(newSchemas.get(schemaName));
            Set<String> added = differenceSet(newFields, oldFields);
            Set<String> removed = differenceSet(oldFields, newFields);
            String renameFrom = renameFrom(newSchemas.get(schemaName), added);
            if (renameFrom == null && oldSchemas.get(schemaName) != null
                    && newSchemas.get(schemaName) != null && added.size() == 1 && removed.size() == 1) {
                String candidate = added.iterator().next();
                String oldField = removed.iterator().next();
                Schema oldProperty = (Schema) oldSchemas.get(schemaName).getProperties().get(oldField);
                Schema newProperty = (Schema) newSchemas.get(schemaName).getProperties().get(candidate);
                if (sameShape(oldProperty, newProperty)) {
                    renameFrom = oldField;
                }
            }
            if (renameFrom != null && removed.contains(renameFrom) && added.size() == 1) {
                renamedFields.add(new ApiSpecDiff.FieldRename(schemaName, renameFrom, added.iterator().next()));
                added.clear();
                removed.remove(renameFrom);
            }
            added.forEach(field -> addedFields.add(new ApiSpecDiff.FieldChange(schemaName, field)));
            removed.forEach(field -> removedFields.add(new ApiSpecDiff.FieldChange(schemaName, field)));
        }
        return new ApiSpecDiff(addedEndpoints, removedEndpoints, addedFields, removedFields, renamedFields);
    }

    private static OpenAPI read(Path path) {
        var result = new OpenAPIV3Parser().readLocation(path.toAbsolutePath().toString(), null, null);
        if (result == null || result.getOpenAPI() == null) {
            throw new IllegalArgumentException("Unable to parse OpenAPI document: " + path);
        }
        return result.getOpenAPI();
    }

    private static Set<String> endpointKeys(OpenAPI api) {
        Set<String> keys = new LinkedHashSet<>();
        if (api.getPaths() != null) {
            api.getPaths().forEach((path, item) -> {
                if (item.getGet() != null) keys.add("GET " + path);
                if (item.getPost() != null) keys.add("POST " + path);
                if (item.getPut() != null) keys.add("PUT " + path);
                if (item.getDelete() != null) keys.add("DELETE " + path);
                if (item.getPatch() != null) keys.add("PATCH " + path);
                if (item.getHead() != null) keys.add("HEAD " + path);
                if (item.getOptions() != null) keys.add("OPTIONS " + path);
                if (item.getTrace() != null) keys.add("TRACE " + path);
            });
        }
        return keys;
    }

    private static Map<String, Schema> schemas(OpenAPI api) {
        return api.getComponents() == null || api.getComponents().getSchemas() == null
                ? Map.of() : api.getComponents().getSchemas();
    }

    private static Set<String> properties(Schema schema) {
        return schema == null || schema.getProperties() == null
                ? new LinkedHashSet<>() : new LinkedHashSet<>(schema.getProperties().keySet());
    }

    private static String renameFrom(Schema schema, Set<String> added) {
        if (schema == null || schema.getProperties() == null || added.isEmpty()) return null;
        for (String field : added) {
            Schema property = (Schema) schema.getProperties().get(field);
            if (property != null && property.getExtensions() != null) {
                Object value = property.getExtensions().get("x-renamed-from");
                if (value instanceof String) {
                    return (String) value;
                }
            }
        }
        return null;
    }

    private static boolean sameShape(Schema oldProperty, Schema newProperty) {
        return oldProperty != null && newProperty != null
                && Objects.equals(oldProperty.getType(), newProperty.getType())
                && Objects.equals(oldProperty.getFormat(), newProperty.getFormat())
                && Objects.equals(oldProperty.get$ref(), newProperty.get$ref());
    }

    private static <T> Set<T> union(Set<T> left, Set<T> right) {
        Set<T> result = new LinkedHashSet<>(left);
        result.addAll(right);
        return result;
    }

    private static <T> List<T> difference(Set<T> left, Set<T> right) {
        return List.copyOf(differenceSet(left, right));
    }

    private static <T> Set<T> differenceSet(Set<T> left, Set<T> right) {
        Set<T> result = new LinkedHashSet<>(left);
        result.removeAll(right);
        return result;
    }
}
