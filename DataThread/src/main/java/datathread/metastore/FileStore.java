package datathread.metastore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import datathread.Indentifier;

public class FileStore implements Metastore {
    Path baseDir;

    public FileStore(Path baseDir) {
        this.baseDir = baseDir;
    }

    public <T> Optional<T> resolveAndRead(Indentifier id, Class<T> tipe) {
        Path filePath = resolveForID(this.baseDir, id, tipe);
        return FileStore.loadFromFile(filePath, tipe);
    }

    public <T> List<T> findAllAndRead(Class<T> metaType) {
        String fileSuffix = FileStore.classNameToFileStyle(metaType);
        List<T> results = Collections.emptyList();

        try (Stream<Path> walk = Files.walk(baseDir)) {
            results = walk.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(fileSuffix + ".json"))
                .map(path -> FileStore.loadFromFile(path, metaType))
                .filter(Optional::isPresent)
                .map(o -> o.get())
                .collect(Collectors.toList())
                ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return results;
    }

    public <T> Optional<String> write(Indentifier id, Class<T> tipe, T data) {
        Path absPath = resolveForID(this.baseDir, id, tipe);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);

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
        return tipe.getSimpleName().replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    public static Path resolveForID(Path baseDir, Indentifier id, Class tipe) {
        return resolveFile(baseDir, id.schema(), id.domain(), id.name(), classNameToFileStyle(tipe));
    }

    public static Path resolveFile(Path baseDir, String schema, String[] domain, String name, String fileSuffix) {
        Path path = Path.of(".", domain).resolve(name + "." + fileSuffix + ".json");
        Path absPath = baseDir.resolve(schema).resolve(path);
        return absPath;
    }

    public static <T> Optional<T> loadFromFile(Path path, Class<T> tipe) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            String json = new String(bytes);
            ObjectMapper mapper = new ObjectMapper();
            T obj = mapper.readValue(json, tipe);

            return Optional.of(obj);
        } catch (IOException e) {
            e.printStackTrace();
            return  Optional.empty();
        }
    }

    public static void main(String[] args) {
        // Create a FileStore instance with the base directory
        Path baseDir = Path.of("example/metastore");
        FileStore fileStore = new FileStore(baseDir);

        // Test the findAllAndRead method
        List<Element> results = fileStore.findAllAndRead(Element.class);
        for (Element result : results) {
            System.out.println(result);
        }
    }
}