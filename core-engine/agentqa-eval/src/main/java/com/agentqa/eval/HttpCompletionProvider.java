package com.agentqa.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/** Live evaluation provider backed by a JSON-returning LLM endpoint. */
public final class HttpCompletionProvider implements CompletionProvider {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final URI endpoint;
    private final String apiKey;

    public HttpCompletionProvider() {
        this(HttpClient.newHttpClient(), new ObjectMapper(),
                URI.create(System.getenv().getOrDefault("AGENTQA_LLM_ENDPOINT",
                        "https://api.anthropic.com/v1/messages")),
                System.getenv("ANTHROPIC_API_KEY"));
    }

    public HttpCompletionProvider(HttpClient client, ObjectMapper mapper,
                                  URI endpoint, String apiKey) {
        this.client = Objects.requireNonNull(client, "client");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.apiKey = apiKey;
    }

    @Override
    public EvaluationCase complete(String promptId, String promptVersion, String caseId)
            throws IOException, InterruptedException {
        String prompt = """
                Evaluate AgentQA case %s using prompt %s version %s.
                Return only JSON matching EvaluationCase fields:
                promptId, promptVersion, caseId, breakingChange,
                verifiedAndPassing, targetPasses, siblingRegressionFails,
                proposed, baselineTtrSeconds, agenticTtrSeconds, riskFlag.
                """.formatted(caseId, promptId, promptVersion);
        var body = mapper.createObjectNode();
        body.put("model", System.getenv().getOrDefault("AGENTQA_LLM_MODEL",
                "claude-3-5-sonnet-20240620"));
        body.put("max_tokens", 800);
        body.putArray("messages").addObject().put("role", "user").put("content", prompt);
        var builder = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        if (apiKey != null && !apiKey.isBlank()) builder.header("x-api-key", apiKey);
        var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Live evaluation request failed with HTTP "
                    + response.statusCode());
        }
        JsonNode payload = mapper.readTree(response.body());
        JsonNode text = payload.get("evaluation");
        if (text == null) text = payload.path("content").path(0).path("text");
        if (!text.isTextual()) {
            throw new IOException("Live evaluation response did not contain evaluation JSON");
        }
        String json = text.textValue().trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start < 0 || end < start) throw new IOException("Invalid evaluation JSON");
        return mapper.readValue(json.substring(start, end + 1), EvaluationCase.class);
    }
}
