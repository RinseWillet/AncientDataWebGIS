package com.webgis.ancientdata.application.service.support;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Thin abstraction over external process execution so services that shell out
 * (e.g. pg_dump) can be unit-tested without spawning real processes.
 */
public interface ProcessExecutor {

    /**
     * Runs a command, redirecting stdout to the given output file.
     *
     * @return the process exit code (0 = success)
     */
    int runToFile(List<String> command, Map<String, String> extraEnv, Path outputFile) throws IOException, InterruptedException;
}

