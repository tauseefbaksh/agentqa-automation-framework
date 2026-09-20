package com.agentqa.spec;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One executable UI or API action. Fields not relevant to the selected type
 * remain absent from the JSON document.
 */
@JsonPropertyOrder({"type", "action", "target", "value", "method", "url", "headers", "body"})
public class TestStep {
    private StepType type;
    private String action;
    private String target;
    private String value;
    private String method;
    private String url;
    private Map<String, String> headers = new LinkedHashMap<>();
    private Object body;

    public TestStep() {
    }

    public StepType getType() { return type; }
    public void setType(StepType type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(headers);
    }
    public Object getBody() { return body; }
    public void setBody(Object body) { this.body = body; }
}
