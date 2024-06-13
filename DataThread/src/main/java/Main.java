import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.TypeSpec;
import datathread.metastore.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;

import javax.lang.model.element.Modifier;

public class Main {
    public static void main(String[] args) {
        String json = null;
        try {
            System.out.println(new File(".").getAbsolutePath());
            File baseDir = new File("sharing/example/metastore");
            String id = "element:person:active";
            File file = new File(baseDir, resolveForID(id).getPath());

            json = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();

        try {
            Element element = mapper.readValue(json, Element.class);
            TypeSpec java = elementToJava(element);
            System.out.println(java);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TypeSpec elementToJava(Element element) {
        TypeSpec java = TypeSpec.classBuilder(element.getName())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .build();

        return java;
    }

    public static File resolveForID(String id) {
        String[] parts = id.split(":");
        return resolveFile(parts[0], parts[1], parts[2]);
    }

    public static File resolveFile(String schemaType, String domain, String name) {
        return new File(domain, name + "." + schemaType + ".json");
    }
}
