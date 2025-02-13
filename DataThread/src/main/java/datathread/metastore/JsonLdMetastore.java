package datathread.metastore;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import datathread.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonLdMetastore implements Metastore {
    private final Path basePath;
    private final Map<String, Object> context;

    public JsonLdMetastore(Path basePath, String contextPath) {
        this.basePath = basePath;
        this.context = loadContext(contextPath);
    }

    @Override
    public <T> Optional<T> read(Identifier id, Class<T> type) {
        File file = resolveFile(id, getExtensionForType(type));
        if (!file.exists()) {
            return Optional.empty();
        }

        try (FileReader reader = new FileReader(file)) {
            Object jsonObject = JsonUtils.fromReader(reader);
            Map<String, Object> framedJson = frame(jsonObject);
            return Optional.of((T) JsonUtils.fromString(JsonUtils.toString(framedJson)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + id, e);
        }
    }

    @Override
    public <T> List<T> readAll(Class<T> type) {
        String extension = getExtensionForType(type);
        return findFiles(basePath.toFile(), extension).stream()
                .map(file -> read(fileToId(file), type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public <T> Optional<String> write(Identifier id, T data) {
        File file = resolveFile(id, getExtensionForType(data.getClass()));
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            Map<String, Object> jsonLd = new HashMap<>();
            jsonLd.put("@context", context);
            jsonLd.put("@id", id.toString());
            //TODO
//            jsonLd.putAll(JsonUtils.fromString(JsonUtils.toString(data)));

            JsonUtils.writePrettyPrint(writer, jsonLd);
            return Optional.of(id.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write " + id, e);
        }
    }

    @Override
    public Optional<String> delete(Identifier id) {
        File file = resolveFile(id, "jsonld");
        if (file.exists() && file.delete()) {
            return Optional.of(id.toString());
        }
        return Optional.empty();
    }

    private File resolveFile(Identifier id, String scheme) {
        return basePath.resolve(
                String.join("/", id.domain()) +
                        "/" + id.name() +
                        "." + scheme +
                        ".jsonld"
        ).toFile();
    }

    private Map<String, Object> frame(Object jsonLd) throws Exception {
        Map<String, Object> frame = new HashMap<>();
        frame.put("@context", context);
        frame.put("@embed", "@always");

        return JsonLdProcessor.frame(jsonLd, frame, new JsonLdOptions());
    }

    private Map<String, Object> loadContext(String contextPath) {
        try (FileReader reader = new FileReader(contextPath)) {
            return (Map<String, Object>) JsonUtils.fromReader(reader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load context", e);
        }
    }

    private String getExtensionForType(Class<?> type) {
        return type.getSimpleName().toLowerCase();
    }

    private List<File> findFiles(File directory, String extension) {
        return java.util.Arrays.stream(directory.listFiles((dir, name) -> name.endsWith("." + extension + ".jsonld")))
                .collect(Collectors.toList());
    }

    private Identifier fileToId(File file) {
        String[] parts = file.getName().split("\\.");
        String scheme = parts[1];
        String name = parts[0];
        String[] domain = file.getParentFile().getPath().substring(basePath.toString().length() + 1).split(File.separator);

        return new Identifier(scheme, domain, name);
    }
}