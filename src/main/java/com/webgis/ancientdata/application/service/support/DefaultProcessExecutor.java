package com.webgis.ancientdata.application.service.support;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
public class DefaultProcessExecutor implements ProcessExecutor {

    @Override
    public int runToFile(List<String> command, Map<String, String> extraEnv, Path outputFile) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command)
                .redirectOutput(outputFile.toFile())
                .redirectErrorStream(false);
        builder.environment().putAll(extraEnv);

        Process process = builder.start();
        return process.waitFor();
    }
}

