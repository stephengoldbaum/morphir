package datathread.metastore;


import com.fasterxml.jackson.databind.ObjectMapper;
import datathread.Identifier;
import datathread.grammar.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
public class FileMetastoreTest {

    FileMetastore fileMetastore;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    Identifier identifier;
    @Mock
    Element mockElement;
    @Mock
    Router router;
    @Mock
    Metastore metastore;
    @Mock
    Path baseDir;

    @BeforeEach
    public void setUp() throws Exception {
//        fileMetastore = new FileMetastore(Paths.get("C:\\Users\\panchron\\Projects\\fsdg.FinanceDataManagement\\DataThread\\example\\metastore\\automated"));
        baseDir = Paths.get("example", "metastore", "automated");
        fileMetastore = new FileMetastore(baseDir);
    }

    @Test
    public void testRead() throws IOException {
        String[] domain = {"person"};
        Identifier id = new Identifier("element", domain, "age");
        when(objectMapper.readValue(anyString(), eq(Element.class))).thenReturn(getElement());
        Class<Element> type = Element.class;
        when(metastore.read(id, type)).thenReturn(Optional.of(getElement()));
        Optional<Element> result = fileMetastore.read(id, type);
        assertTrue(result.isPresent());
        assertEquals("element:person:age", result.get().getId());
        assertEquals("Age", result.get().getName());

    }


    public Element getElement() {
        Element element = new Element();
        element.setId("element:person:age");
        element.setName("Age");
        return element;
    }

    //
    @Test
    public void testReadAll() throws IOException {

        when(objectMapper.readValue(anyString(), eq(Element.class))).thenReturn(getElement());
        when(metastore.readAll(Element.class)).thenReturn(List.of(getElement()));
        List<Element> result = fileMetastore.readAll(Element.class);
        assertNotNull(result);
    }

    //
//
    @Test
    public void testWrite() throws IOException {
        String[] domain = {"person"};
        Identifier id = new Identifier("element", domain, "age");
        Element element = getElement();
        Path filePath = FileMetastore.resolveForID(baseDir, id, Element.class);
        assertTrue(Files.exists(filePath));
        Optional<String> result = fileMetastore.write(id, element);
        assertFalse(result.isPresent());
        assertTrue(Files.exists(filePath));
//        verify(objectMapper).writeValueAsString(element);
    }
}