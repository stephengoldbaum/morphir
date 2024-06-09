import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        String baseDirArg = System.getProperty("baseDir", "metastore");
        Path baseDir = Paths.get(baseDirArg).toAbsolutePath().normalize();

        System.out.println("Using base folder: " + baseDir);

        if (!Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SpringApplication.run(Application.class, args);
    }
}

@RestController
class RequestController {

    @PostMapping("/element")
    public void createElement(@RequestBody String body) {
        
        processRequest(body, "element");
    }

    @PostMapping("/dataset")
    public void createDataset(@RequestBody String body) {
        processRequest(body, "dataset");
    }

    private void processRequest(String body, String type) {
        // TODO: Implement the logic to process the request here.
    }


    private void saveToFile(Map<String, Object> artifact) throws IOException {
        // Save as a JSON file in the data folder
        String id = (String) artifact.get("id");
        String[] items = id.split(":");
        String type = items[0];
        Path filePath = Paths.get("data", type + ".json");
        Files.write(filePath, artifact.toString().getBytes());
    }
}
