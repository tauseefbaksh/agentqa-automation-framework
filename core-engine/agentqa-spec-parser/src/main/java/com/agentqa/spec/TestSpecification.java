package com.agentqa.spec;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * A complete, technology-neutral QA test specification.
 */
@JsonPropertyOrder({"name", "description", "steps", "assertions"})
public class TestSpecification {
    private String name;
    private String description;
    private List<TestStep> steps = new ArrayList<>();
    private List<ExpectedAssertion> assertions = new ArrayList<>();

    public TestSpecification() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TestStep> getSteps() {
        return steps;
    }

    @JsonProperty("steps")
    public void setSteps(List<TestStep> steps) {
        this.steps = steps == null ? new ArrayList<>() : new ArrayList<>(steps);
    }

    public List<ExpectedAssertion> getAssertions() {
        return assertions;
    }

    public void setAssertions(List<ExpectedAssertion> assertions) {
        this.assertions = assertions == null ? new ArrayList<>() : new ArrayList<>(assertions);
    }
}
