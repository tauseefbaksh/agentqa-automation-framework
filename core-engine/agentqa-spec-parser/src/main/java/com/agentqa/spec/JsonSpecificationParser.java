package com.agentqa.spec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Reads typed QA specifications from the application classpath.
 *
 * <p>Unknown properties and coercion of incompatible JSON values are rejected
 * so an invalid fixture cannot silently change test behavior.</p>
 */
public class JsonSpecificationParser {
    private final ObjectMapper objectMapper;

    public JsonSpecificationParser() {
        this(JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build());
    }

    public JsonSpecificationParser(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public TestSpecification parseResource(String resourceName) {
        Objects.requireNonNull(resourceName, "resourceName");
        String normalizedName = resourceName.startsWith("/") ? resourceName.substring(1) : resourceName;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(normalizedName);
        if (stream == null) {
            throw new SpecificationParseException("QA specification resource not found: " + resourceName,
                    new IOException("Resource does not exist on the classpath"));
        }
        try (InputStream input = stream) {
            return parse(input, resourceName);
        } catch (IOException exception) {
            throw new SpecificationParseException("Unable to read QA specification resource: " + resourceName,
                    exception);
        }
    }

    public TestSpecification parse(InputStream input) {
        return parse(input, "input stream");
    }

    private TestSpecification parse(InputStream input, String source) {
        Objects.requireNonNull(input, "input");
        try {
            return objectMapper.readValue(input, TestSpecification.class);
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof SpecificationParseException parseException) {
                throw parseException;
            }
            throw new SpecificationParseException("Malformed QA specification from " + source, exception);
        }
    }
}
