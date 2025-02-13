package datathread.frontend;

import datathread.Identifier;
import datathread.frontends.BasicJavaFrontend;
import datathread.grammar.Dataset;
import datathread.grammar.Element;
import datathread.grammar.Field;
import datathread.metastore.Metastore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import datathread.JavaUtils;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class BasicJavaFrontendTest {

    @Mock
    private JavaUtils javaUtils;
    @InjectMocks
    private BasicJavaFrontend basicJavaFrontend;
    @Mock
    private Metastore metastore;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsElement_PrimitiveClass(){
        assertTrue(BasicJavaFrontend.isElement(int.class));
    }

    @Test
    void testProcessClassAsDataset(){
        Class<?> datasetclass = Dataset.class;
        Object result = BasicJavaFrontend.processClass(datasetclass);
        assertNotNull(result);
        Dataset dataset = (Dataset) result;
        assertEquals("Dataset",dataset.getName());
        assertEquals("id", dataset.getFields().get(0).getName());
    }

    @Test
    public void testProcessClassAsElement(){
        Class<?> elementClass = Element.class;
        Object result = BasicJavaFrontend.processClassAsElement(elementClass);
        assertNotNull(result);
        Element element = (Element) result;
        assertEquals("Element",element.getName());
    }

    @Test
    void testProcessFields(){
        // Arrange
        Class<?> clazz = Dataset.class;
        Identifier mockIdentifier = mock(Identifier.class);

        // Act
        List<Field> result = BasicJavaFrontend.processFields(clazz);

        // Assert
        assertNotNull(result);
        assertEquals("id", result.get(0).getName());
    }

}