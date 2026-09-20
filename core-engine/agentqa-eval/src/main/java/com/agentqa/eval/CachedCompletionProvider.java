package com.agentqa.eval;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Replays fixtures/llm-responses/{promptId}/{promptVersion}/{caseId}.json. */
public final class CachedCompletionProvider implements CompletionProvider {
    private final Path root;
    private final ObjectMapper mapper;

    public CachedCompletionProvider(Path root) {
        this(root, new ObjectMapper());
    }

    public CachedCompletionProvider(Path root, ObjectMapper mapper) {
        this.root = root;
        this.mapper = mapper;
    }

    @Override
    public EvaluationCase complete(String promptId, String promptVersion, String caseId)
            throws IOException {
        Path path = root.resolve(promptId).resolve(promptVersion).resolve(caseId + ".json");
        if (!Files.exists(path)) {
            throw new IOException("Deterministic completion not found: " + path);
        }
        return mapper.readValue(path.toFile(), EvaluationCase.class);
    }
}
