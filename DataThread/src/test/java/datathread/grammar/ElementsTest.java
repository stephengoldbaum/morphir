package datathread.grammar;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ElementsTest {

    @Test
    public void testGetElementTypeFromMap() {
        Map<String, Object> elementTypeMap = new HashMap<>();
        elementTypeMap.put("Text", new HashMap<>());

        Optional<ElementType> result = Elements.getElementType(elementTypeMap);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof Elements.Text);
    }

    @Test
    public void testGetElementTypeFromElement() {
        Element element = new Element();
        element.setElementType(new Elements.Text());

        Optional<ElementType> result = Elements.getElementType(element);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof Elements.Text);
    }

    @Test
    public void testGetElementTypeFromElementWithMap() {
        Element element = new Element();
        Map<String, Object> elementTypeMap = new HashMap<>();
        elementTypeMap.put("Text", new HashMap<>());
        element.setElementType(elementTypeMap);

        Optional<ElementType> result = Elements.getElementType(element);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof Elements.Text);
    }

    @Test
    public void testIsDecimal() {
        datathread.grammar.Number number = new datathread.grammar.Number();
        number.setConstraints(new NumberConstraints());
        number.getConstraints().setPrecision(2);

        assertTrue(Elements.isDecimal(number));

        number.getConstraints().setPrecision(0);

        assertFalse(Elements.isDecimal(number));
    }
}