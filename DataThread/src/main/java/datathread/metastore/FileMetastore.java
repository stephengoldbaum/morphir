package datathread.metastore;

import com.fasterxml.jackson.databind.ObjectMapper;
import datathread.Identifier;
import datathread.grammar.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the Metastore interface that uses the file system to store data objects.
 *
 * The FileMetastore maps an Identifier to folders and names files based on the following rules:
 *
 * 1. The base directory is specified during the initialization of the FileMetastore.
 * 2. The Identifier consists of a scheme, domain, and name.
 * 3. The domain is an array of strings that is interpreted into a nested folders within the base directory.
 * 4. The name is used as the filename, with special characters escaped.
 * 5. The file extension is determined by the class type of the object being stored, converted to a file style string.
 *
 * For example, an Identifier with scheme "scheme", domain ["domain1", "domain2"], and name "name" for a class type "MyClass"
 * would be mapped to a file path: /baseDir/domain1/domain2/name.my_class.json
 */
public class FileMetastore implements Metastore {
    /**
     * Base directory for the file metastore.
     */
    Path baseDir;

    /**
     * ObjectMapper instance for JSON serialization and deserialization.
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructor to initialize the FileMetastore with a base directory and an optional ObjectMapper.
     *
     * @param baseDir the base directory for the file metastore
     * @param objectMapper the ObjectMapper instance for JSON operations
     */
    public FileMetastore(Path baseDir, ObjectMapper objectMapper) {
        this.baseDir = baseDir.toAbsolutePath().normalize();
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        Elements.configureObjectMapper(this.objectMapper);
    }

    /**
     * Constructor to initialize the FileMetastore with a base directory and a default ObjectMapper.
     *
     * @param baseDir the base directory for the file metastore
     */
    public FileMetastore(Path baseDir) {
        this(baseDir, null);
    }

    /**
     * Reads an object of the specified type by its Identifier.
     *
     * @param id the Identifier of the object to be read
     * @param tipe the class type of the object to be read
     * @param <T> the type of the object to be read
     * @return an Optional containing the object if found, or an empty Optional if not found
     */
    public <T> Optional<T> read(Identifier id, Class<T> tipe) {
        Path filePath = resolveForID(this.baseDir, id, tipe);
        return loadFromFile(filePath, tipe);
    }

    /**
     * Reads all objects of the specified type.
     *
     * @param metaType the class type of the objects to be read
     * @param <T> the type of the objects to be read
     * @return a list of all objects found
     */
    public <T> List<T> readAll(Class<T> metaType) {
        String fileSuffix = FileMetastore.classNameToFileStyle(metaType);
        List<T> results = Collections.emptyList();

        try (Stream<Path> walk = Files.walk(baseDir)) {
            results = walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(fileSuffix + ".json"))
                    .flatMap(path -> loadFromFile(path, metaType).stream())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Deletes an object by its Identifier.
     *
     * @param id the Identifier of the object to be deleted
     * @return an Optional containing the result of the delete operation, or an empty Optional if no deletion occurred
     */
    @Override
    public Optional<String> delete(Identifier id) {
        try {
            Path filePath = resolveForID(this.baseDir, id, Object.class);
            Files.delete(filePath);
            return Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.of("Failed to delete id " + id);
        }
    }

    /**
     * Writes an object to the metastore.
     *
     * @param id the Identifier of the object to be written
     * @param data the object to be written
     * @param <T> the type of the object to be written
     * @return an Optional containing a message indicating the result of the write operation
     */
    public <T> Optional<String> write(Identifier id, T data) {
        Path absPath = resolveForID(this.baseDir, id, data.getClass());

        try {
            String json = this.objectMapper.writeValueAsString(data);

            Path folder = absPath.getParent();
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            Files.write(absPath, json.getBytes());

            return Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.of("Failed to store id " + id);
        }
    }

    //// Static helpers

    /**
     * Converts a class name to a file style string.
     *
     * @param tipe the class type
     * @return the file style string
     */
    public static String classNameToFileStyle(Class tipe) {
        return toFileStyle(tipe.getSimpleName());
    }

    /**
     * Converts a string to a file style string.
     *
     * @param s the string to be converted
     * @return the file style string
     */
    public static String toFileStyle(String s) {
        return s.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    /**
     * Escapes special characters in a string.
     *
     * @param str the string to be escaped
     * @return the escaped string
     */
    public static String escape(String str) {
        return str
                .replace("%3F", "?")
                .replace(" ", "%20");
    }

    /**
     * Unescapes special characters in a string.
     *
     * @param str the string to be unescaped
     * @return the unescaped string
     */
    public static String unescape(String str) {
        return str
                .replace("%20", " ")
                .replace("?", "%3F");
    }

    /**
     * Resolves a file path for a given Identifier and type.
     *
     * @param baseDir the base directory
     * @param id the Identifier
     * @param tipe the class type
     * @return the resolved file path
     */
    public static Path resolveForID(Path baseDir, Identifier id, Class tipe) {
        return resolveFile(baseDir, id.scheme(), id.domain(), id.name(), classNameToFileStyle(tipe));
    }

    /**
     * Resolves a file path for given parameters.
     *
     * @param baseDir the base directory
     * @param schema the schema
     * @param domain the domain
     * @param name the name
     * @param fileSuffix the file suffix
     * @return the resolved file path
     */
    public static Path resolveFile(Path baseDir, String schema, String[] domain, String name, String fileSuffix) {
        String[] folders = Arrays.stream(domain).map(FileMetastore::unescape).toArray(String[]::new);
        String filename = unescape(name);

        Path path = baseDir.getFileSystem().getPath(".", folders).resolve(filename + "." + fileSuffix + ".json").normalize();
        Path absPath = baseDir.resolve(path).normalize();

        if (!absPath.startsWith(baseDir)) {
            throw new IllegalArgumentException("Attempted directory traversal attack detected");
        }

        return absPath;
    }

    /**
     * Loads an object from a file.
     *
     * @param path the file path
     * @param tipe the class type of the object to be loaded
     * @param <T> the type of the object to be loaded
     * @return an Optional containing the object if found, or an empty Optional if not found
     */
    public <T> Optional<T> loadFromFile(Path path, Class<T> tipe) {
        try {
            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                String json = new String(bytes);
                T obj = this.objectMapper.readValue(json, tipe);

                return Optional.ofNullable(obj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
}