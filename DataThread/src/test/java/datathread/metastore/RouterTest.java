package datathread.metastore;

import datathread.Identifier;
import datathread.grammar.Element;
import datathread.grammar.ElementInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.*;

@ExtendWith(MockitoExtension.class)

public class RouterTest {

    @Mock
    Identifier identifier;

    @Mock
    private Router router;
    @Mock
    private Map<Class, Metastore> routes;
    @Mock
    private Metastore metastore;

    @BeforeEach
    void setUp(){
        router = new Router(routes);
    }

    @Test
    void testRouterConstructor(){
        assertNotNull(router);
    }

    @Test
    public void testRead(){
        Identifier id = identifier;
        Class<Element> type = Element.class;
        when(routes.get(type)).thenReturn(metastore);
        when(metastore.read(id,type)).thenReturn(Optional.of(getElement()));
        Optional<Element> result = router.read(id,type);
        assertTrue(result.isPresent());
        assertEquals("element:person:age",result.get().getId());
        assertEquals("Age",result.get().getName());
    }

    public Element getElement(){
        Element element = new Element();
        element.setId("element:person:age");
        element.setName("Age");
        return element;
    }

    @Test
    public void testReadAll(){
        Class<Element> type = Element.class;
        when(routes.get(type)).thenReturn(metastore);
        when(metastore.readAll(type)).thenReturn(List.of(getElement()));

        List<Element> result = router.readAll(type);
        assertEquals("element:person:age",result.get(0).getId());
        assertEquals("Age",result.get(0).getName());
    }


    @Test
    public void testWrite(){
        Identifier id = identifier;
        Class<ElementInfo> type = ElementInfo.class;

        when(routes.get(type)).thenReturn(metastore);
        when(metastore.write(id,getElementInfo())).thenReturn(Optional.of("Success"));

        Optional<String> result = router.write(id,getElementInfo());
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("Success",result.get());
        verify(metastore).write(id,getElementInfo());
    }

//    @Test
//    public void testWrite() {
//        Identifier id = new Identifier("scheme", new String[]{"domain"}, "name");
//        Element element = new Element();
//
//        when(metastore.write(id, element)).thenReturn(Optional.empty());
//
//        Optional<String> result = router.write(id, element);
//
//        assertFalse(result.isPresent());
//    }

    public ElementInfo getElementInfo(){
        ElementInfo elementInfo = new ElementInfo();
        elementInfo.setId("element:person:age");
        elementInfo.setDescription("The age of the person in years");
        elementInfo.setDisplayName("Age");
        return elementInfo;
    }
}