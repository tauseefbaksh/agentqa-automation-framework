package com.agentqa.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Thread-safe JSON-lines logger; existing entries are never rewritten or deleted. */
public final class HealingMemoryImpl implements HealingMemory {
    private final Path logPath;
    private final ObjectMapper objectMapper;

    public HealingMemoryImpl() {
        this(Path.of("fixtures", "healing-memory.json"));
    }

    public HealingMemoryImpl(Path logPath) {
        this(logPath, new ObjectMapper().disable(SerializationFeature.INDENT_OUTPUT));
    }

    public HealingMemoryImpl(Path logPath, ObjectMapper objectMapper) {
        this.logPath = Objects.requireNonNull(logPath, "logPath");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public synchronized void append(PatchMemoryEntry entry) throws IOException {
        Objects.requireNonNull(entry, "entry");
        Path parent = logPath.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        String line = objectMapper.writeValueAsString(entry) + System.lineSeparator();
        Files.writeString(logPath, line, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.APPEND);
    }

    @Override
    public synchronized List<PatchMemoryEntry> similarTo(String diffDescription, int limit)
            throws IOException {
        Objects.requireNonNull(diffDescription, "diffDescription");
        if (limit < 0) throw new IllegalArgumentException("limit must not be negative");
        if (!Files.exists(logPath) || limit == 0) return List.of();
        List<PatchMemoryEntry> entries = new ArrayList<>();
        for (String line : Files.readAllLines(logPath)) {
            if (!line.isBlank()) entries.add(objectMapper.readValue(line, PatchMemoryEntry.class));
        }
        String query = diffDescription.toLowerCase();
        return entries.stream()
                .filter(entry -> sharesMeaningfulToken(entry, query))
                .sorted(Comparator.comparing(PatchMemoryEntry::outcome))
                .limit(limit)
                .toList();
    }

    private static boolean sharesMeaningfulToken(PatchMemoryEntry entry, String query) {
        String candidate = (entry.oldPattern() + " " + entry.newPattern() + " "
                + entry.diffType()).toLowerCase();
        return List.of(query.split("[^a-z0-9_/-]+")).stream()
                .filter(token -> token.length() > 2)
                .anyMatch(candidate::contains);
    }

    public Path logPath() {
        return logPath;
    }
}
