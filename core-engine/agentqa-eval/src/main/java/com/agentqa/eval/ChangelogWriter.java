package com.agentqa.eval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/** Appends traceable evaluation rows to the Phase 2 improvement changelog. */
public final class ChangelogWriter {
    public void append(Path changelog, String stage, String rationale,
                        String command, Path metricsArtifact, String decision)
            throws IOException {
        Objects.requireNonNull(changelog, "changelog");
        String row = "| " + clean(stage) + " | " + clean(rationale) + " | command: `"
                + clean(command) + "`; artifact: `" + clean(metricsArtifact.toString()) + "` | "
                + clean(decision) + " |" + System.lineSeparator();
        Path parent = changelog.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        if (!Files.exists(changelog)) {
            Files.writeString(changelog,
                    "# Improvement Changelog" + System.lineSeparator()
                            + System.lineSeparator()
                            + "| Stage | What you tried and why | Evidence | Decision/learning |"
                            + System.lineSeparator()
                            + "|---|---|---|---|" + System.lineSeparator(),
                    StandardOpenOption.CREATE);
        }
        Files.writeString(changelog, row, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static String clean(String value) {
        return Objects.requireNonNull(value, "value").replace("|", "\\|")
                .replace("\r", " ").replace("\n", " ");
    }
}
