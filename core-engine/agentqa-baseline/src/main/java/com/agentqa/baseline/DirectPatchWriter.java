package com.agentqa.baseline;

import java.io.IOException;
import java.nio.file.Path;

/** Writes the baseline's unverified patch directly into the failing test. */
@FunctionalInterface
public interface DirectPatchWriter {
    void apply(String patch, Path failingTest) throws IOException;
}
