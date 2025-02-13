package datathread.metastore;

import datathread.Identifier;
import datathread.grammar.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class FederatedMetastoreTest {

    @Mock
    private Metastore delegate1;
    @Mock
    private Metastore delegate2;
    private FederatedMetastore federatedMetastore;

    @BeforeEach
    public void setUp() {
        federatedMetastore = new FederatedMetastore(Arrays.asList(delegate1, delegate2));
    }

    @Test
    public void testRead() {
        Identifier id = new Identifier("scheme", new String[]{"domain"}, "name");
        Element element = new Element();

        when(delegate1.read(id, Element.class)).thenReturn(Optional.empty());
        when(delegate2.read(id, Element.class)).thenReturn(Optional.of(element));

        Optional<Element> result = federatedMetastore.read(id, Element.class);

        assertTrue(result.isPresent());
        assertEquals(element, result.get());
    }

    @Test
    public void testReadAll() {
        Element element1 = new Element();
        Element element2 = new Element();

        when(delegate1.readAll(Element.class)).thenReturn(Collections.singletonList(element1));
        when(delegate2.readAll(Element.class)).thenReturn(Collections.singletonList(element2));

        List<Element> result = federatedMetastore.readAll(Element.class);

        assertEquals(2, result.size());
        assertTrue(result.contains(element1));
        assertTrue(result.contains(element2));
    }

    @Test
    public void testWrite() {
        Identifier id = new Identifier("scheme", new String[]{"domain"}, "name");
        Element element = new Element();

        Optional<String> result = federatedMetastore.write(id, element);

        assertTrue(result.isPresent());
        assertEquals("Write is not supported for FederatedMetastore.", result.get());
    }
}