package datathread.metastore;

import datathread.Identifier;
import datathread.grammar.Element;
import datathread.grammar.ElementType;
import datathread.grammar.Elements;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RDFMetastoreTest {
    private RDFMetastore rdfMetastore;
    private Repository repository;
    private ObjectMapper mockMapper;

    @BeforeEach
    public void setUp() {
        mockMapper = mock(ObjectMapper.class);
        repository = new SailRepository(new MemoryStore());
        repository.init();

        rdfMetastore = new RDFMetastore(repository, mockMapper);
    }

    @Test
    public void testRead() throws Exception {
        Element element = TestUtils.basicTextElement("test", "domain");
        Identifier id = Identifier.from(element.getId()).orElseThrow();
        ElementType elementType = new Elements.Text();

        // Prepare
        rdfMetastore.write(id, element);

        Optional<Element> result = rdfMetastore.read(id, Element.class);
        assertTrue(result.isPresent());
        assertEquals(element, result.get());
    }

    @Test
    public void testReadAll() throws Exception {
        // Prepare
        for(int i : new int[]{1, 2, 3}) {
            Element element = TestUtils.basicTextElement("test" + i, "domain");
            Identifier id = Identifier.from(element.getId()).orElseThrow();
            ElementType elementType = new Elements.Text();

            // Prepare
            rdfMetastore.write(id, element);
        }

        List<Element> result = rdfMetastore.readAll(Element.class);
        assertFalse(result.isEmpty());

        // TODO: Add more assertions
    }

    @Test
    public void testWrite() throws Exception {
        Identifier id = new Identifier("element", new String[] {"tests"}, "test-id");
        String data = "value";
        String jsonValue = "{\"key\":\"value\"}";

        when(mockMapper.writeValueAsString(data)).thenReturn(jsonValue);

        Optional<String> result = rdfMetastore.write(id, data);
        assertTrue(result.isPresent());
        assertEquals(id.toString(), result.get());
    }

    @Test
    public void testDelete() throws Exception {
        Identifier id = new Identifier("element", new String[] {"tests"}, "test-id");
        String jsonValue = "{\"key\":\"value\"}";

        try (RepositoryConnection conn = repository.getConnection()) {
            conn.prepareUpdate(rdfMetastore.buildInsertStatement(id, jsonValue)).execute();
        }

        Optional<String> result = rdfMetastore.delete(id);
        assertTrue(result.isPresent());
        assertEquals(id.toString(), result.get());
    }
}