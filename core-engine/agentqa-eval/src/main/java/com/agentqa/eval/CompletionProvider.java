package com.agentqa.eval;

import java.io.IOException;

@FunctionalInterface
public interface CompletionProvider {
    EvaluationCase complete(String promptId, String promptVersion, String caseId)
            throws IOException, InterruptedException;
}
