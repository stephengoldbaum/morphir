package datathread.metastore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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

public class FileMetastore implements Metastore {
    Path baseDir;

    private final ObjectMapper objectMapper;

    public FileMetastore(Path baseDir, ObjectMapper objectMapper) {
        this.baseDir = baseDir;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        Elements.configureObjectMapper(this.objectMapper);
    }

    public FileMetastore(Path baseDir) {
        this(baseDir, null);
    }

    public <T> Optional<T> read(Identifier id, Class<T> tipe) {
        Path filePath = resolveForID(this.baseDir, id, tipe);
        return loadFromFile(filePath, tipe);
    }

    public <T> List<T> readAll(Class<T> metaType) {
        String fileSuffix = FileMetastore.classNameToFileStyle(metaType);
        List<T> results = Collections.emptyList();

        try (Stream<Path> walk = Files.walk(baseDir)) {
            results = walk.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(fileSuffix + ".json"))
                .flatMap(path -> loadFromFile(path, metaType).stream())
                .collect(Collectors.toList())
                ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return results;
    }

    public <T> Optional<String> write(Identifier id, T data) {
        Path absPath = resolveForID(this.baseDir, id, data.getClass());

        try {
            String json = this.objectMapper.writeValueAsString(data);

            Path folder = absPath.getParent();
            if(!Files.exists(folder)) {
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
    public static String classNameToFileStyle(Class tipe) {
        return toFileStyle(tipe.getSimpleName());
    }

    public static String toFileStyle(String s) {
        return s.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    public static String unescape(String str) {
        return str.replace("%20", " ");
    }

    public static Path resolveForID(Path baseDir, Identifier id, Class tipe) {
        return resolveFile(baseDir, id.scheme(), id.domain(), id.name(), classNameToFileStyle(tipe));
    }

    public static Path resolveFile(Path baseDir, String schema, String[] domain, String name, String fileSuffix) {
        String[] folders = Arrays.stream(domain).map(FileMetastore::unescape).toArray(String[]::new);
        String filename = unescape(name);

        Path path = Path.of(".", folders).resolve(filename + "." + fileSuffix + ".json");
        Path absPath = baseDir.resolve(path);
        return absPath;
    }

    public <T> Optional<T> loadFromFile(Path path, Class<T> tipe) {
        try {
            if(Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                String json = new String(bytes);
                T obj = this.objectMapper.readValue(json, tipe);

                return Optional.ofNullable(obj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  Optional.empty();
    }
}
