package com.agentqa.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * HTTP-backed healer. The request contains the failing source, the implicated
 * diff, and at most three memory entries; the response is always classified
 * locally so an LLM cannot bypass the safety contract.
 */
public final class SelfHealerImpl implements SelfHealer {
    private static final String DEFAULT_ENDPOINT = "https://api.anthropic.com/v1/messages";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI endpoint;
    private final String apiKey;
    private final HealingMemory memory;

    public SelfHealerImpl(HealingMemory memory) {
        this(HttpClient.newHttpClient(), new ObjectMapper(),
                URI.create(System.getenv().getOrDefault("AGENTQA_LLM_ENDPOINT", DEFAULT_ENDPOINT)),
                System.getenv("ANTHROPIC_API_KEY"), memory);
    }

    public SelfHealerImpl(HttpClient httpClient, URI endpoint, String apiKey,
                          HealingMemory memory) {
        this(httpClient, new ObjectMapper(), endpoint, apiKey, memory);
    }

    public SelfHealerImpl(HttpClient httpClient, ObjectMapper objectMapper, URI endpoint,
                          String apiKey, HealingMemory memory) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.apiKey = apiKey;
        this.memory = Objects.requireNonNull(memory, "memory");
    }

    @Override
    public HealingProposal propose(String failingTestSource, String diffDescription)
            throws IOException, InterruptedException {
        Objects.requireNonNull(failingTestSource, "failingTestSource");
        Objects.requireNonNull(diffDescription, "diffDescription");
        List<PatchMemoryEntry> similar = memory.similarTo(diffDescription, 3);
        String prompt = prompt(failingTestSource, diffDescription, similar);
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", System.getenv().getOrDefault("AGENTQA_LLM_MODEL", "claude-3-5-sonnet-20240620"));
        requestBody.put("max_tokens", 1200);
        requestBody.putArray("messages").addObject().put("role", "user").put("content", prompt);

        HttpRequest.Builder request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)));
        if (apiKey != null && !apiKey.isBlank()) request.header("x-api-key", apiKey);
        HttpResponse<String> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("LLM request failed with HTTP " + response.statusCode());
        }
        String patch = extractPatch(response.body());
        return new HealingProposal(patch, classify(patch));
    }

    private String prompt(String source, String diff, List<PatchMemoryEntry> memoryEntries) {
        return """
                Fix only the failing test below. Return a minimal source patch or replacement line,
                never a rewritten test. Change only a selector, endpoint path, payload mapping, or
                assertion needed by the supplied diff.
                Failing test:
                %s
                Diff:
                %s
                Similar past patches:
                %s
                """.formatted(source, diff, memoryEntries);
    }

    private String extractPatch(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode patch = root.get("patch");
        if (patch != null && patch.isTextual()) return patch.textValue().trim();
        JsonNode content = root.path("content");
        if (content.isArray() && !content.isEmpty()) {
            JsonNode text = content.get(0).get("text");
            if (text != null && text.isTextual()) return text.textValue().trim();
        }
        if (root.isTextual()) return root.textValue().trim();
        throw new IOException("LLM response did not contain a textual patch");
    }

    static PatchType classify(String patch) {
        String normalized = patch.toLowerCase(Locale.ROOT);
        boolean assertion = normalized.matches("(?s).*\\b(assert|assertthat|expect|verify|should|equals|contains|hasvalue)\\b.*");
        return assertion ? PatchType.ASSERTION_CHANGE : PatchType.SELECTOR_OR_PAYLOAD_FIX;
    }
}
