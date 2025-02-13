package datathread.grammar;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import datathread.Identifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

/**
 * Class representing various element types used in the grammar.
 *
 * This class exists to provide a structured way to handle different types of elements within the grammar.
 * It encapsulates the logic for creating, managing, and deserializing these elements, making it easier for developers
 * to work with the grammar without needing to understand the underlying implementation details.
 *
 * Developers should use this class when they need to:
 * 1. Define or manipulate different element types within the grammar.
 * 2. Deserialize JSON representations of elements into their corresponding Java objects.
 * 3. Ensure consistency and type safety when working with various element types.
 */
public class Elements implements ElementType {

    // Nested classes representing specific element types
    public static class Enum extends datathread.grammar.Enum implements ElementType {}
    public static class Number extends datathread.grammar.Number implements ElementType {}
    public static class Record extends datathread.grammar.Record implements ElementType {}
    public static class Reference extends datathread.grammar.Reference implements ElementType {}
    public static class Text extends datathread.grammar.Text implements ElementType {}
    public static class Boolean implements ElementType {}
    public static class Date implements ElementType {}
    public static class Time implements ElementType {}
    public static class DateTime implements ElementType {}

    /**
     * Retrieves an ElementType based on the provided map.
     *
     * @param elementType a map containing element type information
     * @return an Optional containing the ElementType if found, or an empty Optional if not found
     */
    public static Optional<ElementType> getElementType(Map<String, Object> elementType) {
        Optional<ElementType> result = elementType.keySet().stream().findFirst()
                .map(tipe -> {
                    if (tipe != null) {
                        switch (tipe) {
                            case "Boolean":
                                return new Elements.Boolean();
                            case "Date":
                                return new Elements.Date();
                            case "DateTime":
                                return new Elements.DateTime();
                            case "Enum":
                                return new Enum();
                            case "Number":
                                Elements.Number number = new Elements.Number();
                                Object rawConstraints = elementType.get("Number");

                                if (rawConstraints instanceof Map) {
                                    Map<String, Object> constraints = (Map<String, Object>) rawConstraints;

                                    number.setConstraints(new NumberConstraints());

                                    if (constraints.get("maximum") instanceof Integer) {
                                        number.getConstraints().setMaximum((Integer) constraints.get("maximum"));
                                    }

                                    if (constraints.get("minimum") instanceof Integer) {
                                        number.getConstraints().setMinimum((Integer) constraints.get("minimum"));
                                    }

                                    if (constraints.get("precision") instanceof Integer) {
                                        number.getConstraints().setPrecision((Integer) constraints.get("precision"));
                                    }
                                }

                                return number;
                            case "Record":
                                return new Record();
                            case "Reference":
                                return new Reference();
                            case "Text":
                                return new Text();
                            default:
                                return null;
                        }
                    }
                    return null;
                });

        return result;
    }

    /**
     * Retrieves an ElementType based on the provided element.
     *
     * @param element the element containing element type information
     * @return an Optional containing the ElementType if found, or an empty Optional if not found
     */
    public static Optional<ElementType> getElementType(Element element) {
        Object et = element.getElementType();

        if (et instanceof ElementType)
            return Optional.of((ElementType) et);
        else if (et instanceof Map)
            return getElementType((Map<String, Object>) et);
        else
            return Optional.empty();
    }

    /**
     * Checks if the provided number is a decimal.
     *
     * @param number the number to check
     * @return true if the number is a decimal, false otherwise
     */
    public static boolean isDecimal(datathread.grammar.Number number) {
        int precision = 0;

        if (number.getConstraints() != null && number.getConstraints().getPrecision() != null) {
            precision = number.getConstraints().getPrecision();
        }

        return precision != 0;
    }

    /**
     * Custom deserializer for ElementType.
     */
    public static class ElementTypeDeserializer extends JsonDeserializer<ElementType> {
        @Override
        public ElementType deserialize(JsonParser jp, DeserializationContext ctxt) {
            try {
                JsonNode elementNode = jp.getCodec().readTree(jp);
                JsonNode elementTypeNode = elementNode.get("element_type");
                String elementTypeName = elementTypeNode == null ? null :
                        elementTypeNode.properties().stream().findFirst().map(i -> i.getKey()).orElse(null);

                if (elementTypeName == null) {
                    return null;
                }

                JsonNode typeNode = elementTypeNode.get(elementTypeName);

                switch (elementTypeName) {
                    case "Boolean":
                        return new Elements.Boolean();
                    case "Date":
                        return new Elements.Date();
                    case "DateTime":
                        return new Elements.DateTime();
                    case "Enum":
                        return new Elements.Enum();
                    case "Number":
                        Elements.Number number = new Elements.Number();
                        number.setConstraints(new NumberConstraints());

                        if (typeNode.has("maximum") && typeNode.get("maximum").isInt()) {
                            number.getConstraints().setMaximum(typeNode.get("maximum").asInt());
                        }
                        if (typeNode.has("minimum") && typeNode.get("minimum").isInt()) {
                            number.getConstraints().setMinimum(typeNode.get("minimum").asInt());
                        }
                        if (typeNode.has("precision") && typeNode.get("precision").isInt()) {
                            number.getConstraints().setPrecision(typeNode.get("precision").asInt());
                        }
                        return number;
                    case "Record":
                        return new Elements.Record();
                    case "Reference":
                        return new Elements.Reference();
                    case "Text":
                        Elements.Text textElement = new Elements.Text();

                        if (typeNode.has("minLength") || typeNode.has("maxLength")) {
                            TextConstraints constraints = new TextConstraints();

                            if (typeNode.has("maxLength")) {
                                constraints.setMaxLength(typeNode.get("maxLength").asInt());
                            }
                            if (typeNode.has("minLength")) {
                                constraints.setMinLength(typeNode.get("minLength").asInt());
                            }

                            textElement.setConstraints(constraints);
                        }

                        return textElement;
                    default:
                        return null;
                }
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * Configures the provided ObjectMapper to use the custom ElementTypeDeserializer.
     *
     * @param mapper the ObjectMapper to configure
     */
    public static void configureObjectMapper(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ElementType.class, new ElementTypeDeserializer());
        mapper.registerModule(module);
    }

    /**
     * Converts a class to its corresponding ElementType.
     *
     * @param clazz the class to convert
     * @return the corresponding ElementType, or null if no match is found
     */
    public static ElementType classToElementType(Class clazz) {
        if (clazz.isPrimitive()) {
            switch (clazz.getSimpleName()) {
                case "short":
                    return new Elements.Number(); // TODO precision
                case "int":
                    return new Elements.Number(); // TODO precision
                case "long":
                    return new Elements.Number(); // TODO precision
                case "float":
                    return new Elements.Number(); // TODO precision
                case "double":
                    return new Elements.Number(); // TODO precision
                case "boolean":
                    return new Elements.Boolean(); // TODO precision
            }
        }

        if (clazz.isEnum()) {
            return new Elements.Enum(); // TODO values
        }

        if (clazz.getPackage().getName().startsWith("java.lang")) {
            if (clazz == String.class) {
                return new Elements.Text();
            }
            if (clazz == Character.class) {
                return new Elements.Text();
            }
            if (clazz == Byte.class) {
                return new Elements.Number(); // TODO precision
            }
            if (clazz == Short.class) {
                return new Elements.Number(); // TODO precision
            }
            if (clazz == Integer.class) {
                return new Elements.Number(); // TODO precision
            }
            if (clazz == Long.class) {
                return new Elements.Number(); // TODO precision
            }
            if (clazz == Float.class) {
                return new Elements.Number(); // TODO precision
            }
            if (clazz == Double.class) {
                return new Elements.Number(); // TODO precision
            }
            if (clazz == java.lang.Boolean.class) {
                return new Elements.Boolean(); // TODO precision
            }
            if (clazz == BigDecimal.class) {
                return new Elements.Number(); // TODO precision
            }
            if (clazz == LocalDate.class) {
                return new Elements.Date(); // TODO precision
            }
            if (clazz == LocalDateTime.class) {
                return new Elements.DateTime(); // TODO precision
            }
            if (clazz == LocalTime.class) {
                return new Elements.Time(); // TODO precision
            }
        }

        return null;
    }
}