package com.yourname.hospital.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourname.hospital.config.ElasticsearchProperties;
import com.yourname.hospital.dto.SearchRecordPageResponse;
import com.yourname.hospital.dto.SearchRecordResponse;
import com.yourname.hospital.entity.MetadataRecord;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchService {

    private final ElasticsearchProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ElasticsearchService(ElasticsearchProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = buildHttpClient();
        ensureIndex();
    }

    private HttpClient buildHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        String caCertPath = properties.getCaCertPath();
        if (caCertPath != null && !caCertPath.isBlank()) {
            try {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                try (var input = Files.newInputStream(Path.of(caCertPath))) {
                    var cert = factory.generateCertificate(input);
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", cert);
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                            TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(keyStore);
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
                    builder.sslContext(sslContext);
                }
            } catch (Exception ignored) {
                // Fall back to JVM default trust store.
            }
        }
        return builder.build();
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public void ensureIndex() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            HttpRequest head = baseRequest(indexUri()).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<Void> response = httpClient.send(head, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == 404) {
                createIndex();
            }
        } catch (Exception ignored) {
            // Avoid failing startup when Elasticsearch is offline.
        }
    }

    public void indexMetadata(MetadataRecord metadata) throws IOException, InterruptedException {
        if (!properties.isEnabled() || metadata == null || metadata.getRecord() == null) {
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("recordId", metadata.getRecord().getId());
        body.put("patientCode", metadata.getPatientCode());
        body.put("patientName", metadata.getPatientName());
        body.put("keywords", metadata.getKeywords());
        body.put("summary", metadata.getSummary());
        body.put("updatedAt", metadata.getUpdatedAt().toString());
        String payload = objectMapper.writeValueAsString(body);
        HttpRequest request = baseRequest(indexUri(metadata.getRecord().getId()))
                .PUT(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Elasticsearch index failed: " + response.body());
        }
    }

    public void deleteMetadata(Long recordId) throws IOException, InterruptedException {
        if (!properties.isEnabled() || recordId == null) {
            return;
        }
        HttpRequest request = baseRequest(indexUri(recordId))
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300 && response.statusCode() != 404) {
            throw new IllegalStateException("Elasticsearch delete failed: " + response.body());
        }
    }

    public List<SearchRecordResponse> searchByKeyword(String keyword, int limit)
            throws IOException, InterruptedException {
        SearchRecordPageResponse page = searchRecordsPage(keyword, null, null, 0, limit);
        return page.getItems();
    }

    public List<SearchRecordResponse> searchRecords(
            String keyword,
            String patientCode,
            Long recordId,
            int limit) throws IOException, InterruptedException {
        SearchRecordPageResponse page = searchRecordsPage(keyword, patientCode, recordId, 0, limit);
        return page.getItems();
    }

    public SearchRecordPageResponse searchRecordsPage(
            String keyword,
            String patientCode,
            Long recordId,
            int page,
            int size) throws IOException, InterruptedException {
        if (!properties.isEnabled()) {
            return new SearchRecordPageResponse(List.of(), 0, page, size);
        }

        Map<String, Object> bool = new HashMap<>();
        List<Object> filters = new ArrayList<>();
        if (patientCode != null && !patientCode.isBlank()) {
            filters.add(Map.of("term", Map.of("patientCode", patientCode)));
        }
        if (recordId != null) {
            filters.add(Map.of("term", Map.of("recordId", recordId)));
        }
        if (!filters.isEmpty()) {
            bool.put("filter", filters);
        }
        if (keyword != null && !keyword.isBlank()) {
            Map<String, Object> match = new HashMap<>();
            match.put("query", keyword);
            match.put("fields", List.of("patientName^2", "keywords", "summary"));
            bool.put("must", List.of(Map.of("multi_match", match)));
        }

        Map<String, Object> query = bool.isEmpty()
                ? Map.of("match_all", Map.of())
                : Map.of("bool", bool);

        Map<String, Object> body = new HashMap<>();
        body.put("from", Math.max(0, page) * Math.max(1, size));
        body.put("size", Math.max(1, size));
        body.put("query", query);
        body.put("sort", List.of(Map.of("updatedAt", Map.of("order", "desc"))));
        body.put("track_total_hits", true);

        String payload = objectMapper.writeValueAsString(body);
        HttpRequest request = baseRequest(searchUri())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Elasticsearch search failed: " + response.body());
        }
        return parseSearchPage(response.body(), page, size);
    }

    private SearchRecordPageResponse parseSearchPage(String payload, int page, int size) throws IOException {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode hitsNode = root.path("hits");
        JsonNode hits = hitsNode.path("hits");
        long total = 0;
        JsonNode totalNode = hitsNode.path("total");
        if (totalNode.isObject()) {
            total = totalNode.path("value").asLong(0);
        } else if (totalNode.isNumber()) {
            total = totalNode.asLong(0);
        }
        if (!hits.isArray()) {
            return new SearchRecordPageResponse(List.of(), total, page, size);
        }
        List<SearchRecordResponse> results = new ArrayList<>();
        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            Long recordId = source.path("recordId").isNumber() ? source.path("recordId").longValue() : null;
            results.add(new SearchRecordResponse(
                    recordId,
                    asTextOrNull(source, "patientCode"),
                    asTextOrNull(source, "patientName"),
                    asTextOrNull(source, "keywords"),
                    asTextOrNull(source, "summary"),
                    asTextOrNull(source, "updatedAt")
            ));
        }
        return new SearchRecordPageResponse(results, total, page, size);
    }

    private String asTextOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private void createIndex() throws IOException, InterruptedException {
        Map<String, Object> mappings = new HashMap<>();
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("recordId", Map.of("type", "long"));
        propertiesMap.put("patientCode", Map.of("type", "keyword"));
        propertiesMap.put("patientName", Map.of("type", "text"));
        propertiesMap.put("keywords", Map.of("type", "text"));
        propertiesMap.put("summary", Map.of("type", "text"));
        propertiesMap.put("updatedAt", Map.of("type", "date"));
        mappings.put("properties", propertiesMap);

        String payload = objectMapper.writeValueAsString(Map.of("mappings", mappings));
        HttpRequest request = baseRequest(indexUri())
                .PUT(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/json")
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder baseRequest(URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            builder.header("Authorization", "ApiKey " + properties.getApiKey().trim());
        } else if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            String raw = properties.getUsername().trim() + ":" +
                    (properties.getPassword() == null ? "" : properties.getPassword());
            String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
            builder.header("Authorization", "Basic " + encoded);
        }
        return builder;
    }

    private URI indexUri() {
        return URI.create(properties.getUrl() + "/" + properties.getIndex());
    }

    private URI indexUri(Long id) {
        return URI.create(properties.getUrl() + "/" + properties.getIndex() + "/_doc/" + id);
    }

    private URI searchUri() {
        return URI.create(properties.getUrl() + "/" + properties.getIndex() + "/_search");
    }
}
