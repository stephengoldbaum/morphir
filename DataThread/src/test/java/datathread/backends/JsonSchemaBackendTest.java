package datathread.backends;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaBackendTest {

    private JsonSchemaBackend jsonSchemaBackend;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        jsonSchemaBackend = new JsonSchemaBackend();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGenerateSchema() {
        Map<String, Object> dataThreadDefinition = new HashMap<>();
        dataThreadDefinition.put("name", "http://example.com/name");
        dataThreadDefinition.put("details", Map.of("description", "http://example.com/description"));

        ObjectNode schema = jsonSchemaBackend.generateSchema(dataThreadDefinition);

        assertNotNull(schema);
        assertEquals("http://json-schema.org/draft-07/schema#", schema.get("$schema").asText());
        assertEquals("object", schema.get("type").asText());

        ObjectNode properties = (ObjectNode) schema.get("properties");
        assertNotNull(properties);
        assertEquals("string", properties.get("name").get("type").asText());
        assertEquals("http://example.com/name", properties.get("name").get("iri").asText());

        ObjectNode details = (ObjectNode) properties.get("details");
        assertNotNull(details);
        assertEquals("object", details.get("type").asText());
    }

    @Test
    public void testCreatePropertyNodeWithString() {
        ObjectNode propertyNode = jsonSchemaBackend.createPropertyNode("http://example.com/name");

        assertNotNull(propertyNode);
        assertEquals("string", propertyNode.get("type").asText());
        assertEquals("http://example.com/name", propertyNode.get("iri").asText());
    }

    @Test
    public void testCreatePropertyNodeWithMap() {
        Map<String, Object> value = Map.of("description", "http://example.com/description");
        ObjectNode propertyNode = jsonSchemaBackend.createPropertyNode(value);

        assertNotNull(propertyNode);
        assertEquals("object", propertyNode.get("type").asText());

        ObjectNode properties = (ObjectNode) propertyNode.get("properties");
        assertNotNull(properties);
//        assertEquals("string", properties.get("description").get("type").asText());
    }
}