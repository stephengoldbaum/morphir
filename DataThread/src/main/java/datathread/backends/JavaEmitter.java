package datathread.backends;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import datathread.Identifier;
import datathread.metastore.Metastore;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaEmitter {
    private final Metastore  context;

    public JavaEmitter(Metastore context) {
        this.context = context;
    }

    public TypeSpec handleDataset(Dataset dataset) {
        String name = nameToJava(dataset.getName());
        List<FieldSpec> fields =
            dataset.getFields().stream()
                .map(field -> handleField(field))
                .collect(Collectors.toList());

        TypeSpec javaClass = TypeSpec.classBuilder(name)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fields)
            .build();

        return javaClass;
    }

    public FieldSpec handleField(Field field) {
        String name = field.getName();
        Optional<Element> element =
            Identifier.from(field.getElement())
                .flatMap(id -> context.read(id, Element.class))
                .map(Element.class::cast); // Cast the Optional<Object> to Optional<Element>

        ClassName className = element
            .map(e -> handleElementType(e.getElementType()))
            .orElse(ClassName.get(Object.class));

        return FieldSpec.builder(className, name)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .build();
    }

    public ClassName handleElementType(ElementType elementType) {
        String tipe = elementType.getAdditionalProperties().keySet().toArray()[0].toString();

        // TODO
        switch(tipe) {
            case "Boolean":
                return ClassName.get(Boolean.class);
            case "Date":
                return ClassName.get(java.time.LocalDate.class);
            case "DateTime":
                return ClassName.get(java.time.LocalDateTime.class);
            case "Enum":
                return idToClassName(elementType.getAdditionalProperties().get("name").toString());
            case "Number":
                return ClassName.get(Double.class);
            case "Reference":
                String ref = "" + elementType.getAdditionalProperties().get("Reference");
                return idToClassName(ref);
            case "Text":
                return ClassName.get(String.class);
            default:
                return ClassName.get(Object.class);
            }

//        if(cls == Enum.class)
//            return String.class;
//        else if(cls == Text.class)
//            return String.class;
//        else if(cls == Number.class)
//            return Double.class;
////        else if(cls == Record.class)
////        else if(cls == Reference.class)
////            return String.class;
//        else if(cls == Text.class)
//            return String.class;
//        else
//            return Object.class;
    }

    public TypeSpec handleEnum(ElementType e) {
        String name = e.getAdditionalProperties().getOrDefault("name", "Enum").toString();

        TypeSpec java = TypeSpec.enumBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .build();

        return java;
    }

    public TypeSpec handleElement(Element element) {
        TypeSpec java = TypeSpec.classBuilder(element.getName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .build();

        return java;
    }

    //// Utils ////
    public static ClassName idToClassName(String id) {
        String[] split = id.split(":");

        String pkg = nameToJava(split[1]);
        String name = nameToJava(split[2]);

        return ClassName.get(pkg, name);
    }
    public static String nameToJava(String name) {
        return Arrays.stream(name.split("\\s+"))
           .map(JavaEmitter::capitalize)
                .collect(Collectors.joining("_"));
    }

    public static String capitalize(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
