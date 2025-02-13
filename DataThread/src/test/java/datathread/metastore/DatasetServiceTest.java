package datathread.metastore;

import datathread.Identifier;
import datathread.grammar.Dataset;
import datathread.grammar.Element;
import datathread.grammar.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DatasetServiceTest {

    private Metastore metastore;
    private DatasetService datasetService;

    @BeforeEach
    public void setUp() {
        metastore = mock(Metastore.class);
        datasetService = new DatasetService(metastore);
    }

    @Test
    public void testGet() {
        Identifier id = new Identifier("scheme", new String[]{"domain"}, "name");
        Dataset dataset = new Dataset();
        Field field = new Field();
        field.setName("fieldName");
        dataset.setFields(Arrays.asList(field));

        when(metastore.read(id, Dataset.class)).thenReturn(Optional.of(dataset));
        when(metastore.read(new Identifier("scheme", new String[]{"domain"}, "fieldName"), Element.class))
                .thenReturn(Optional.of(new Element()));

        Optional<Dataset> result = datasetService.get(id);

        assertTrue(result.isPresent());
        assertEquals(dataset, result.get());
//        assertNotNull(result.get().getFields().get(0).getElement());
    }
}