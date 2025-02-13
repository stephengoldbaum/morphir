package datathread.backends;

import com.squareup.javapoet.*;
import datathread.Identifier;
import datathread.grammar.*;
import datathread.metastore.Metastore;
import datathread.metastore.Router;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;

@MockitoSettings(strictness =  Strictness.LENIENT)
public class JavaEmitterTest {


    @InjectMocks
    JavaEmitter javaEmitter;

    @Mock
    private Metastore context;

    @Mock
    Dataset mockDataset;

    @Mock
    Field mockField;
    @Mock
    Element mockElement;



    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        when(mockField.getName()).thenReturn(getField().getName());
        when(mockField.getElement()).thenReturn(getField().getElement());
        when(mockDataset.getId()).thenReturn(getDataset().getId());
        when(mockDataset.getName()).thenReturn(getDataset().getName());
        when(mockDataset.getFields()).thenReturn(List.of(mockField));
        when(mockElement.getId()).thenReturn(getElement().getId());
        when(mockElement.getName()).thenReturn(getElement().getName());

    }

    @Test
    public void handleElementTest(){

        TypeSpec result = javaEmitter.handleDataset(mockDataset);

        assertNotNull(result);
        verify(mockDataset).getName();
        assertEquals("People", result.name);
        assertEquals(1, result.fieldSpecs.size());
        assertEquals("fieldname", result.fieldSpecs.get(0).name);
    }

    @Test
    public void testHandleField(){
        when(context.read(any(),eq(Element.class))).thenReturn(Optional.empty());

        FieldSpec result = javaEmitter.handleField(mockField);

        assertNotNull(result);
        verify(mockField).getName();
        assertEquals("fieldname", result.name);
    }

    @Test
    public void testHandleElement(){


        TypeSpec result = javaEmitter.handleElement(mockElement);

        assertNotNull(result);
        assertEquals("Integer", result.name);
        assertEquals(1, result.fieldSpecs.size());
        assertEquals("value", result.fieldSpecs.get(0).name);
    }

    @Test
    public void testGetElementType(){
        ElementType elementType = new Elements.Text();

        ClassName result = javaEmitter.handleElementType(elementType);
        assertNotNull(result);
        assertEquals(ClassName.get(String.class), result);

    }

    @Test
    public void testProcess() {

        when(context.readAll(Element.class)).thenReturn(Collections.singletonList(mockElement));
        when(context.readAll(Dataset.class)).thenReturn(Collections.singletonList(mockDataset));

        List<JavaFile> result = JavaEmitter.process(context);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    public Element getElement(){
        Element element = new Element();
        element.setId("element:core:integer");
        element.setName("Integer");
        return element;
    }

    public Dataset getDataset(){
        Dataset dataset = new Dataset();
        dataset.setId("dataset:person:people");
        dataset.setName("People");
        dataset.setVersion("1");
        return dataset;
    }

    public Field getField(){
        Field field = new Field();
        field.setElement(getElement().getId());
        field.setName("fieldname");
        return field;
    }

}