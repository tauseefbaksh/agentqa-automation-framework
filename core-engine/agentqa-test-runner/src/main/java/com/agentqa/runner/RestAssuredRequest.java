package com.agentqa.runner;

import io.restassured.response.Response;

import java.util.Map;
import java.util.Objects;

/** Small REST Assured request description for executor-owned API checks. */
public record RestAssuredRequest(String method, String url, Map<String, ?> body) {
    public RestAssuredRequest {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(url, "url");
        if (method.isBlank() || url.isBlank()) {
            throw new IllegalArgumentException("method and url must not be blank");
        }
        body = body == null ? Map.of() : Map.copyOf(body);
    }

    public Response execute() {
        var request = io.restassured.RestAssured.given();
        if (body != null) request.body(body);
        return request.request(method, url);
    }
}
