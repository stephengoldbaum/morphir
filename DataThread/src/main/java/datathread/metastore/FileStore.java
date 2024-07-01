package datathread.metastore;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class FileStore implements Metastore {
    File baseDir;

    public FileStore(File baseDir) {
        this.baseDir = baseDir;
    }

    public T Optional<T> resolveAndRead(String id, Class<T> tipe) {
        return read(baseDir, id, tipe);
    }

    public T List<T> findAllAndRead(Class<T> tipe) {
        String metaType = tipe.getSimpleName().toLowerCase(); // TODO: Names library for consistency
        return readAll(this.baseDir, metaType, tipe);
    }

    public <T,Error> Optional<Error> write(String id, String tipe, T data) {
        return write(this.baseDir, id, data);
    }

    public Optional<Element> readElement(String elementId) {
        return read(baseDir, elementId, Element.class);
    }

    public static File resolveForID(String id) {
        String[] parts = id.split(":");
        return resolveFile(parts[0], parts[1], parts[2]);
    }

    public static File resolveFile(String schemaType, String domain, String name) {
        return new File(domain, name + "." + schemaType + ".json");
    }

    public static <T> List<T> readAll(File baseDir, String metaType, Class<T> type) {
        List<T> items = new ArrayList<>();
        String pattern = metaType + ".json";

        try (Stream<Path> paths = Files.walk(Paths.get(baseDir.getPath()))) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(pattern))
                    .forEach(path -> {
                        try {
                            String json = new String(Files.readAllBytes(path));
                            ObjectMapper mapper = new ObjectMapper();
                            T item = mapper.readValue(json, type);
                            items.add(item);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static List<Element> readAllElements(File baseDir) {
        return readAll(baseDir, "element", Element.class);
    }

    public static List<Dataset> readAllDatasets(File baseDir) {
        return readAll(baseDir, "dataset", Dataset.class);
    }

    public static <T> Optional<T> read(File baseDir, String id, Class<T> cls) {
        File elementFile = resolveForID(id);
        File absPath = new File(baseDir, elementFile.getPath());

        try {
            String json = new String(Files.readAllBytes(Paths.get(absPath.getAbsolutePath())));
            ObjectMapper mapper = new ObjectMapper();
            T item = mapper.readValue(json, cls);
            return Optional.of(item);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Either<File, String> write(File baseDir, String id, Object obj) {
        File elementFile = resolveForID(id);
        File absPath = new File(baseDir, elementFile.getPath());

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(obj);

            const folder = absPath.getParentFile();
            if(!folder.exists()) {
                folder.mkdirs();
            }

            Files.write(Paths.get(absPath.getAbsolutePath()), json.getBytes());
            return Either.left(absPath);
        } catch (IOException e) {
            e.printStackTrace();
            return Either.right("Failed to store id " + id);
        }
    }

    public static void main(String[] args) {
        File baseDir = new File("sharing/example/metastore");


        System.out.println("==== Elements ====");
        List<Element> elements = readAllElements(baseDir);
        for (Element element : elements) {
            System.out.println(element.getElementType());
        }

        System.out.println("==== Datasets ====");
        List<Dataset> datasets = readAllDatasets(baseDir);
        for (Dataset dataset : datasets) {
            System.out.println(dataset.getId());
        }


    }
}
