package com.example.aichat.openapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "openapi.output", matches = "\\S+")
class OpenApiContractExportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportsCanonicalOpenApiContractWhenOutputPathIsProvided() throws Exception {
        String output = System.getProperty("openapi.output");

        String responseBody = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode canonicalContract = sortRecursively(objectMapper.readTree(responseBody));
        Path outputPath = Path.of(output).toAbsolutePath().normalize();
        Files.createDirectories(outputPath.getParent());
        Files.writeString(
                outputPath,
                objectMapper.writeValueAsString(canonicalContract) + "\n",
                StandardCharsets.UTF_8
        );
    }

    private JsonNode sortRecursively(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = objectMapper.createObjectNode();
            List<String> fieldNames = new ArrayList<>();
            fieldNames.addAll(node.propertyNames());
            fieldNames.stream()
                    .sorted(Comparator.naturalOrder())
                    .forEach(fieldName -> sorted.set(fieldName, sortRecursively(node.get(fieldName))));
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode sorted = objectMapper.createArrayNode();
            node.forEach(element -> sorted.add(sortRecursively(element)));
            return sorted;
        }
        return node;
    }
}
