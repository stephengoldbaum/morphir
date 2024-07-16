package datathread.metastore;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import datathread.Indentifier;

public class FileStore implements Metastore {
    File baseDir;

    public FileStore(File baseDir) {
        this.baseDir = baseDir;
    }

    public <T> Optional<T> resolveAndRead(Indentifier id, Class<T> tipe) {
        return read(baseDir, id, tipe);
    }

    public <T> List<T> findAllAndRead(Class<T> tipe) {
        String metaType = tipe.getSimpleName().toLowerCase(); // TODO: Names library for consistency
        return readAll(this.baseDir, metaType, tipe);
    }

    @Override
    public <T> Optional<String> write(Indentifier id, String tipe, T data) {
        Path elementFile = resolveForID(id);
        Path absPath = Files.(this.baseDir.getAbsolutePath(), elementFile);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(obj);

            File folder = absPath.getParentFile();
            if(!folder.exists()) {
                folder.mkdirs();
            }

            Files.write(Paths.get(absPath.getAbsolutePath()), json.getBytes());
            return Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.of("Failed to store id " + id);
        }
    }

    public static Path resolveForID(Indentifier id) {
        return resolveFile(id.schema(), id.domain(), id.name());
    }

    public static Path resolveFile(String schema, String[] domain, String name) {
        Path path = Path.of(".", domain);

        return Files.find(path, 0, , null, null)
    }

    public static <T> List<T> readAll(File baseDir, String metaType, Class<T> type) {
        // ... rest of the method
    }

    public static <T> Optional<T> read(File baseDir, String id, Class<T> cls) {
        // ... rest of the method
    }

    public static void main(String[] args) {
        // ... rest of the method
    }
}