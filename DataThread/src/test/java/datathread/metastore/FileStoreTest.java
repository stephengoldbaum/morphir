package datathread.metastore;

import datathread.Identifier;
import datathread.metastore.FileStore;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import datathread.metastore.Element;

public class FileStoreTest {
    private final Path basedir = Path.of(".");

    @Test
    public void testResolveForID() {
        String scheme = "scheme";
        String[] domain = {"domain"};
        String name = "name";
        Identifier id = new Identifier(scheme, domain, name);

        Path actual = FileStore.resolveForID(basedir, id, Element.class);

        final Path expected = Path.of(".", domain[0], name + ".element.json");
        assertEquals(expected, actual);
    }

    @Test
    public void testResolveFile() {
        String schema = "schema";
        String[] domain = {"domain"};
        String name = "name";
        String suffix = "element";

        Path actual = FileStore.resolveFile(basedir, schema, domain, name, suffix);

        final Path expected = Paths.get(".", domain[0], name + "." + suffix + ".json");
        assertEquals(expected, actual);
    }
}
