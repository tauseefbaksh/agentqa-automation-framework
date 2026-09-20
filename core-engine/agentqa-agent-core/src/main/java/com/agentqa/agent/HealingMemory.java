package com.agentqa.agent;

import java.io.IOException;
import java.util.List;

/** Append-only storage for verified and rejected healing patterns. */
public interface HealingMemory {
    void append(PatchMemoryEntry entry) throws IOException;
    List<PatchMemoryEntry> similarTo(String diffDescription, int limit) throws IOException;
}
