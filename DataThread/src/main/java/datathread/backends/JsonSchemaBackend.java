package datathread.backends;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class JsonSchemaBackend {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ObjectNode generateSchema(Map<String, Object> dataThreadDefinition) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        for (Map.Entry<String, Object> entry : dataThreadDefinition.entrySet()) {
            properties.set(entry.getKey(), createPropertyNode(entry.getValue()));
        }
        schema.set("properties", properties);

        return schema;
    }

    ObjectNode createPropertyNode(Object value) {
        ObjectNode propertyNode = objectMapper.createObjectNode();
        if (value instanceof String) {
            propertyNode.put("type", "string");
            propertyNode.put("iri", (String) value);
        } else if (value instanceof Map) {
            propertyNode.put("type", "object");
            propertyNode.set("properties", generateSchema((Map<String, Object>) value));
        }
        return propertyNode;
    }
}
