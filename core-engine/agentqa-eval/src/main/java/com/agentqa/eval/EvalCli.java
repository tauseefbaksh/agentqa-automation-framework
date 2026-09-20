package com.agentqa.eval;

import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Minimal Phase 2 CLI; deterministic replay is the default. */
public final class EvalCli {
    private EvalCli() {
    }

    public static void main(String[] args) throws Exception {
        List<String> arguments = new ArrayList<>(List.of(args));
        if (Boolean.getBoolean("agentqa.appendChangelog")
                && !arguments.contains("--append-changelog")) {
            arguments.add("--append-changelog");
        }
        EvaluationMode mode = arguments.contains("--live")
                ? EvaluationMode.LIVE : EvaluationMode.DETERMINISTIC;
        Path cache = option(arguments, "--cache-root", defaultPath(
                Path.of("fixtures", "llm-responses"),
                Path.of("..", "fixtures", "llm-responses")));
        Path artifact = option(arguments, "--artifact", defaultPath(
                Path.of("results", "metrics.json"),
                Path.of("..", "results", "metrics.json")));
        Path changelog = option(arguments, "--changelog", defaultPath(
                Path.of("docs", "CHANGELOG.md"),
                Path.of("..", "docs", "CHANGELOG.md")));
        EvaluationHarness harness = new EvaluationHarness();
        List<EvaluationCase> cases = discover(cache);
        if (cases.isEmpty()) {
            throw new IllegalStateException("No evaluation cases found under " + cache);
        }
        if (mode == EvaluationMode.LIVE) {
            List<EvaluationMetrics> runs = new ArrayList<>();
            for (int iteration = 0; iteration < 3; iteration++) {
                runs.add(harness.evaluate(mode, cases, cache, new HttpCompletionProvider()));
            }
            harness.writeLiveSummary(LiveEvaluationSummary.calculate(runs), artifact);
            if (arguments.contains("--append-changelog")) {
                for (int iteration = 1; iteration <= 3; iteration++) {
                    appendRow(harness, changelog, mode, arguments, artifact,
                            "Live iteration " + iteration + " completed.");
                }
            }
        } else {
            harness.writeMetrics(harness.evaluate(mode, cases, cache, new CachedCompletionProvider(cache)),
                    artifact);
            if (arguments.contains("--append-changelog")) {
                appendRow(harness, changelog, mode, arguments, artifact,
                        "Deterministic replay retained for byte-identical evaluation.");
            }
        }
        System.out.println("Mode: " + mode + "; metrics: " + artifact);
    }

    private static void appendRow(EvaluationHarness harness, Path changelog,
                                  EvaluationMode mode, List<String> arguments,
                                  Path artifact, String decision) throws IOException {
        String command = "./evaluate.sh " + String.join(" ", arguments);
        harness.appendChangelog(changelog, mode.name(), "Evaluated AgentQA cases",
                command, artifact, decision);
    }

    private static Path option(List<String> args, String name, Path defaultValue) {
        int index = args.indexOf(name);
        return index >= 0 && index + 1 < args.size() ? Path.of(args.get(index + 1)) : defaultValue;
    }

    private static Path defaultPath(Path repositoryPath, Path reactorPath) {
        return Files.exists(repositoryPath) ? repositoryPath : reactorPath;
    }

    private static List<EvaluationCase> discover(Path root) throws IOException {
        if (!Files.exists(root)) return List.of();
        List<EvaluationCase> cases = new ArrayList<>();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json")).toList()) {
                cases.add(new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(path.toFile(), EvaluationCase.class));
            }
        }
        return cases;
    }
}
