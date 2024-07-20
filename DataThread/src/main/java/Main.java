import com.ms.data.casc.manifest.model.SystemDataElement;
import com.squareup.javapoet.TypeSpec;
import datathread.Identifier;
import datathread.backends.JavaEmitter;
import datathread.metastore.FileStore;
import datathread.metastore.Metastore;
import datathread.metastore.Router;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        // Create separate stores for Dataset, Element, and ElementInfo
        Metastore exampleMetastore = new FileStore(Paths.get("DataThread", "example", "metastore", "automated"));
        final Metastore buildMetastore = new FileStore(Paths.get("DataThread", "build", "metastore", "automated"));

        // Setup the router
        final Router router = new Router(Map.of(
                Element.class, exampleMetastore
                , ElementInfo.class, buildMetastore
                , Dataset.class, exampleMetastore
        ));

        // Setup the test domain and subject
        final String[] domain = {"person"};
        final Identifier id = new Identifier("element", domain, "age");

        // Read Age Element from examples
        Optional<Element> age = router.read(id, Element.class);
        System.out.println("Element: " + age);

        // Augment Age Element by saving a related ElementInfo
        ElementInfo info = new ElementInfo();
        info.setId(Identifier.toString(id));
        info.setDescription("The age of the person in years");
        info.setDisplayName("Age");
        Optional<String> response = router.write(id, ElementInfo.class, info);
        System.out.println("Response: " + response);

        // Read it back
        Optional<ElementInfo> outfo = router.read(id, ElementInfo.class);
        System.out.println("OUtfo: " + outfo);

        // Write to Java
        JavaEmitter javaEmitter = new JavaEmitter(router);
        Optional<TypeSpec> java = age.map(e -> javaEmitter.handleElement(e));
        System.out.println("Element Java:");
        System.out.println(java);

        // Again with a Dataset
        Optional<Dataset> people = router.read(new Identifier("dataset", domain, "people"), Dataset.class);
        Optional<TypeSpec> javaD = people
                .map(o -> javaEmitter.handleDataset(o));
        System.out.println();
        System.out.println("Dataset Java:");
        System.out.println(javaD);
    }

}
