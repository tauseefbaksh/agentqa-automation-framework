package com.agentqa.baseline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/**
 * Fair one-shot baseline: one prompt, one LLM response, direct write-back.
 * It intentionally has no planner, memory, verification, or risk metadata.
 */
public final class OneShotBaseline {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final URI endpoint;
    private final String apiKey;
    private final DirectPatchWriter patchWriter;

    public OneShotBaseline(DirectPatchWriter patchWriter) {
        this(HttpClient.newHttpClient(), new ObjectMapper(),
                URI.create(System.getenv().getOrDefault("AGENTQA_LLM_ENDPOINT",
                        "https://api.anthropic.com/v1/messages")),
                System.getenv("ANTHROPIC_API_KEY"), patchWriter);
    }

    public OneShotBaseline(HttpClient client, URI endpoint, String apiKey,
                           DirectPatchWriter patchWriter) {
        this(client, new ObjectMapper(), endpoint, apiKey, patchWriter);
    }

    public OneShotBaseline(HttpClient client, ObjectMapper mapper, URI endpoint,
                           String apiKey, DirectPatchWriter patchWriter) {
        this.client = Objects.requireNonNull(client, "client");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.apiKey = apiKey;
        this.patchWriter = Objects.requireNonNull(patchWriter, "patchWriter");
    }

    public String fix(Path failingTest, String diffDescription)
            throws IOException, InterruptedException {
        Objects.requireNonNull(failingTest, "failingTest");
        String source = java.nio.file.Files.readString(failingTest);
        String prompt = "Fix this failing test directly. Return only the minimal patch or replacement source.\n"
                + "Failing test:\n" + source + "\nDiff:\n" + diffDescription;
        var body = mapper.createObjectNode();
        body.put("model", System.getenv().getOrDefault("AGENTQA_LLM_MODEL",
                "claude-3-5-sonnet-20240620"));
        body.put("max_tokens", 1200);
        body.putArray("messages").addObject().put("role", "user").put("content", prompt);
        var request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        if (apiKey != null && !apiKey.isBlank()) request.header("x-api-key", apiKey);
        var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("LLM request failed with HTTP " + response.statusCode());
        }
        String patch = extractPatch(response.body());
        patchWriter.apply(patch, failingTest);
        return patch;
    }

    private String extractPatch(String body) throws IOException {
        JsonNode root = mapper.readTree(body);
        JsonNode patch = root.get("patch");
        if (patch != null && patch.isTextual()) return patch.textValue().trim();
        JsonNode content = root.path("content");
        if (content.isArray() && !content.isEmpty() && content.get(0).has("text")) {
            return content.get(0).get("text").textValue().trim();
        }
        if (root.isTextual()) return root.textValue().trim();
        throw new IOException("LLM response did not contain a textual patch");
    }
}
