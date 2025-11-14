package com.nexaworks.rafiq.utils;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@Component
public class OpenAPIDocumentGenerator implements CommandLineRunner {

    @Override
    public void run(String... args) {
        if (args.length > 0 && "generate-openapi".equals(args[0])) {
            generateOpenAPIDocs();
            // Exit after generating docs
            System.exit(0);
        }
    }

    public void generateOpenAPIDocs() {
        try {
            // Create output directory if it doesn't exist
            String outputDir = "openapi";
            Files.createDirectories(Paths.get(outputDir));

            // Use RestTemplate to fetch the OpenAPI JSON
            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "http://localhost:8032/v3/api-docs";

            // Fetch JSON
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
            String json = response.getBody();

            // Write JSON
            Files.write(Paths.get(outputDir, "openapi.json"), json.getBytes());

            // Convert JSON to YAML
            ObjectMapper jsonMapper = new ObjectMapper();
            Object jsonObject = jsonMapper.readValue(json, Object.class);

            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            String yaml = yamlMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonObject);

            // Write YAML
            Files.write(Paths.get(outputDir, "openapi.yaml"), yaml.getBytes());

            System.out.println(
                    "OpenAPI documentation generated successfully in the 'openapi' directory");
        } catch (Exception e) {
            System.err.println("Error generating OpenAPI documentation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
