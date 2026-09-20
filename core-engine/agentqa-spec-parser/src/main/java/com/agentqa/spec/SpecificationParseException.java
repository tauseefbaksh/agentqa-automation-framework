package com.agentqa.spec;

/**
 * Indicates that a QA specification could not be loaded or deserialized.
 */
public class SpecificationParseException extends RuntimeException {
    public SpecificationParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
