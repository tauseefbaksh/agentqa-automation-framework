package com.agentqa.eval;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptRegistryTest {
    @Test
    void documentedPromptVersionsExist() throws Exception {
        Path path = firstExisting(Path.of("docs", "llm-prompts.md"),
                Path.of("..", "docs", "llm-prompts.md"),
                Path.of("..", "..", "docs", "llm-prompts.md"));
        String registry = Files.readString(path);
        for (String prompt : new String[]{"baseline.oneshot.v1.0", "planner.diff.v1.0",
                "healer.patch.v1.0", "executor.selection.v1.0", "verification.guard.v1.0"}) {
            assertTrue(registry.contains(prompt), "Missing prompt registry entry: " + prompt);
        }
    }

    private static Path firstExisting(Path... candidates) {
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) return candidate;
        }
        throw new IllegalStateException("Prompt registry not found");
    }
}
