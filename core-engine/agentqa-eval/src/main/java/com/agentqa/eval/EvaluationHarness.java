package com.agentqa.eval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Runs deterministic cache replay or delegates completion generation to live mode. */
public final class EvaluationHarness {
    private final ObjectMapper mapper;

    public EvaluationHarness() {
        mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public EvaluationMetrics evaluate(EvaluationMode mode, List<EvaluationCase> cases,
                                      Path cacheRoot, CompletionProvider liveProvider)
            throws IOException, InterruptedException {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(cases, "cases");
        CompletionProvider provider = mode == EvaluationMode.DETERMINISTIC
                ? new CachedCompletionProvider(cacheRoot) : Objects.requireNonNull(liveProvider, "liveProvider");
        List<EvaluationCase> results = cases.stream()
                .map(spec -> load(provider, spec))
                .toList();
        return EvaluationMetrics.calculate(results);
    }

    public Path writeMetrics(EvaluationMetrics metrics, Path artifact) throws IOException {
        Path parent = artifact.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        mapper.writeValue(artifact.toFile(), metrics);
        return artifact;
    }

    public Path writeLiveSummary(LiveEvaluationSummary summary, Path artifact) throws IOException {
        Path parent = artifact.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        mapper.writeValue(artifact.toFile(), summary);
        return artifact;
    }

    /** Appends one traceable row containing the exact invocation and artifact path. */
    public void appendChangelog(Path changelog, String stage, String rationale,
                                String exactCommand, Path metricsArtifact,
                                String decision) throws IOException {
        new ChangelogWriter().append(changelog, stage, rationale, exactCommand,
                metricsArtifact, decision);
    }

    private EvaluationCase load(CompletionProvider provider, EvaluationCase spec) {
        try {
            return provider.complete(spec.promptId(), spec.promptVersion(), spec.caseId());
        } catch (IOException | InterruptedException exception) {
            throw new EvaluationFailure("Unable to load evaluation case " + spec.caseId(), exception);
        }
    }

    public static final class EvaluationFailure extends RuntimeException {
        public EvaluationFailure(String message, Exception cause) {
            super(message, cause);
        }
    }
}
