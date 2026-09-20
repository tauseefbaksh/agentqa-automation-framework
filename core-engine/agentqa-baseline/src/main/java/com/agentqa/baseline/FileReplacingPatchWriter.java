package com.agentqa.baseline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Baseline writer that replaces the failing test immediately with the response text. */
public final class FileReplacingPatchWriter implements DirectPatchWriter {
    @Override
    public void apply(String patch, Path failingTest) throws IOException {
        Files.writeString(failingTest, patch);
    }
}
