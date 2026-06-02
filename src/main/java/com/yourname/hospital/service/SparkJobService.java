package com.yourname.hospital.service;

import com.yourname.hospital.config.ElasticsearchProperties;
import com.yourname.hospital.config.SparkProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class SparkJobService {

    private final SparkProperties sparkProperties;
    private final ElasticsearchProperties elasticsearchProperties;
    private final Environment environment;

    public SparkJobService(
            SparkProperties sparkProperties,
            ElasticsearchProperties elasticsearchProperties,
            Environment environment) {
        this.sparkProperties = sparkProperties;
        this.elasticsearchProperties = elasticsearchProperties;
        this.environment = environment;
    }

    public boolean isEnabled() {
        return sparkProperties.isEnabled();
    }

    public SparkJobResult runReindexJob() {
        if (!sparkProperties.isEnabled()) {
            return new SparkJobResult("SPARK_DISABLED", "Spark job is disabled.");
        }
        List<String> command = new ArrayList<>();
        command.add(sparkProperties.getSubmit());
        command.add("--master");
        command.add(sparkProperties.getMaster());
        command.add("--name");
        command.add(sparkProperties.getAppName());
        command.add("--class");
        command.add("com.yourname.hospital.spark.RecordReindexJob");
        command.add(sparkProperties.getJobJar());

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Map<String, String> env = builder.environment();
        env.put("JDBC_URL", environment.getProperty("spring.datasource.url", ""));
        env.put("JDBC_USER", environment.getProperty("spring.datasource.username", ""));
        env.put("JDBC_PASSWORD", environment.getProperty("spring.datasource.password", ""));
        env.put("ES_URL", elasticsearchProperties.getUrl());
        env.put("ES_INDEX", elasticsearchProperties.getIndex());
        if (elasticsearchProperties.getUsername() != null) {
            env.put("ES_USER", elasticsearchProperties.getUsername());
        }
        if (elasticsearchProperties.getPassword() != null) {
            env.put("ES_PASSWORD", elasticsearchProperties.getPassword());
        }
        if (elasticsearchProperties.getCaCertPath() != null) {
            env.put("ES_CA_CERT", elasticsearchProperties.getCaCertPath());
        }

        try {
            Process process = builder.start();
            StringBuilder output = new StringBuilder();
            Thread reader = new Thread(() -> {
                byte[] buffer = new byte[4096];
                try (var input = process.getInputStream()) {
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        if (output.length() >= 8000) {
                            continue;
                        }
                        output.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
                    }
                } catch (IOException ignored) {
                }
            });
            reader.setDaemon(true);
            reader.start();
            int exit = process.waitFor();
            reader.join(1000L);
            String combined = output.toString().trim();
            return new SparkJobResult(exit == 0 ? "SPARK_OK" : "SPARK_FAILED",
                    combined.isEmpty() ? null : combined);
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new SparkJobResult("SPARK_FAILED", ex.getMessage());
        }
    }

    public static class SparkJobResult {
        private final String status;
        private final String output;

        public SparkJobResult(String status, String output) {
            this.status = status;
            this.output = output;
        }

        public String getStatus() {
            return status;
        }

        public String getOutput() {
            return output;
        }
    }
}
