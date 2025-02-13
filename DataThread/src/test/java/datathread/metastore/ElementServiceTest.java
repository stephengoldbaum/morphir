package datathread.metastore;

import datathread.Identifier;
import datathread.grammar.Element;
import datathread.grammar.ElementType;
import datathread.grammar.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ElementServiceTest {

    private Metastore metastore;
    private ElementService elementService;

    @BeforeEach
    public void setUp() {
        metastore = mock(Metastore.class);
        elementService = new ElementService(metastore);
    }

    @Test
    public void testGet() {
        Element element = TestUtils.basicTextElement("test", "domain");
        Identifier id = Identifier.from(element.getId()).orElseThrow();
        ElementType elementType = new Elements.Text();

        when(metastore.read(id, Element.class)).thenReturn(Optional.of(element));
        when(metastore.read(id, ElementType.class)).thenReturn(Optional.of(elementType));

        Optional<Element> result = elementService.get(id);

        assertTrue(result.isPresent());
        assertEquals(element, result.get());
        assertNotNull(result.get().getElementType());
    }

    @Test
    public void testGetElementType() {
        Element element = TestUtils.basicTextElement("test", "domain");
        Identifier id = Identifier.from(element.getId()).orElseThrow();
        ElementType elementType = new Elements.Text();

        when(metastore.read(id, ElementType.class)).thenReturn(Optional.of(elementType));

        Optional<ElementType> result = elementService.getElementType(id);

        assertTrue(result.isPresent());
        assertEquals(elementType, result.get());
    }
}