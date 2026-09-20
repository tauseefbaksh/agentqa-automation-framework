package com.agentqa.spec;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A declarative expectation evaluated after the test steps run.
 */
@JsonPropertyOrder({"type", "path", "operator", "expected"})
public class ExpectedAssertion {
    private String type;
    private String path;
    private String operator;
    private Object expected;

    public ExpectedAssertion() {
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Object getExpected() { return expected; }
    public void setExpected(Object expected) { this.expected = expected; }
}
