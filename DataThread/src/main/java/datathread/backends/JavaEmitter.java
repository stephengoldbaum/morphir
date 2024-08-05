package datathread.backends;

import com.squareup.javapoet.*;
import datathread.Identifier;
import datathread.JavaUtils;
import datathread.annotations.URN;
import datathread.grammar.*;
import datathread.grammar.Number;
import datathread.metastore.Metastore;
import datathread.metastore.MetastoreCLIProcessor;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class JavaEmitter {
    private final Metastore  context;

    public JavaEmitter(Metastore context) {
        this.context = context;
    }

    public TypeSpec handleDataset(Dataset dataset) {
        String name = JavaUtils.escapeToJava(dataset.getName());

        // Member fields
        List<FieldSpec> fields =
            dataset.getFields().stream()
                .map(field -> handleField(field))
                .collect(Collectors.toList());

        // Constructor
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC);
        dataset.getFields().stream()
                .map(field -> handleField(field))
                .forEach(field -> {
                    constructorBuilder.addParameter(field.type, field.name);
                    constructorBuilder.addStatement("this.$N = $N", field.name, field.name);
                });
        MethodSpec constructor = constructorBuilder.build();

        // Annotation
        AnnotationSpec annotationSpec = AnnotationSpec.builder(URN.class)
                .addMember("value", "$S", dataset.getId())
                .build();

        // Build
        TypeSpec javaClass = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fields)
                .addMethod(constructor)
                .addAnnotation(annotationSpec)
                .build();

        return javaClass;
    }

    public FieldSpec handleField(Field field) {
        String name = field.getName();

        // Get the Element from the Field
        Optional<Element> element =
            Identifier.from(field.getElement().toString())
                .flatMap(id -> context.read(id, Element.class))
                .map(Element.class::cast); // Cast the Optional<Object> to Optional<Element>

        // Get the ElementType
//        ClassName elementType = element
//                .flatMap(e -> getElementType(e))
//                .map(e -> handleElementType(e))
//                .orElse(ClassName.get(Object.class));
        ClassName elementTypeSpec = element
                .map(e -> e.getId())
                .flatMap(Identifier::from)
                .map(JavaUtils::idToFQN)
                .orElse(ClassName.get(Object.class));

        // Annotation
        AnnotationSpec annotationSpec = AnnotationSpec.builder(URN.class)
                .addMember("value", "$S", field.getElement())
                .build();

        return FieldSpec.builder(elementTypeSpec, name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(annotationSpec)
                .build();
    }

    protected Optional<ElementType> getElementType(Element element) {
        ElementType et = Elements.getElementType(element).orElse(null);

        if( et == null ) {
            et = Identifier.from(element.getId().toString())
                .flatMap(id -> context.read(id, ElementType.class))
                .orElse(null);
            element.setElementType(et);
        }

        return Optional.ofNullable(et);
    }

    public ClassName handleElementType(ElementType elementType) {
        if(elementType != null) {
            String tipe = elementType.getClass().getSimpleName();

            // TODO
            switch (tipe) {
                case "Boolean":
                    return ClassName.get(Boolean.class);
                case "Date":
                    return ClassName.get(java.time.LocalDate.class);
                case "DateTime":
                    return ClassName.get(java.time.LocalDateTime.class);
                //            case "Enum":
                //                return idToClassName(elementType.getAdditionalProperties().get("name").toString());
                case "Number":
                    return Elements.isDecimal((Number) elementType) ?
                        ClassName.get(Double.class) : ClassName.get(Integer.class);
                //            case "Reference":
                //                String ref = "" + elementType.getAdditionalProperties().get("Reference");
                //                return idToClassName(ref);
                case "Text":
                    return ClassName.get(String.class);
                default:
                    // TODO: Need to do the others
                    System.out.println("Unknown type: " + tipe + ", using Object");
                    return ClassName.get(Object.class);
            }
        }

        return ClassName.get(Object.class);
    }

    public TypeSpec handleEnum(Object e) {
        // TODO
        return null;

//        String name = e.getAdditionalProperties().getOrDefault("name", "Enum").toString();
//
//        TypeSpec java = TypeSpec.enumBuilder(name)
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//                .build();
//
//        return java;
    }

public TypeSpec handleElement(Element element) {
    AnnotationSpec annotationSpec = AnnotationSpec.builder(URN.class)
            .addMember("value", "$S", element.getId())
            .build();

    String className = JavaUtils.escapeToJava(element.getName());
    ClassName tipe = Elements.getElementType(element)
            .map(et -> handleElementType(et))
            .orElse(ClassName.get(Object.class));

    FieldSpec valueMember = FieldSpec.builder(tipe, "value")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .build();

    MethodSpec constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(tipe, "value")
            .addCode("this.value = value;")
            .build();

    TypeSpec java = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(annotationSpec)
            .addField(valueMember)
            .addMethod(constructor)
            .build();

    return java;
}

    //// CLI Main ////
    public static List<JavaFile> process(Metastore context) {
        JavaEmitter emitter = new JavaEmitter(context);

        List<JavaFile> elements = processElements(context, emitter);
        List<JavaFile> datasets = processDatasets(context, emitter);

        return Stream.concat(elements.stream(), datasets.stream())
                .collect(Collectors.toList());
    }

    public static List<JavaFile> processElements(Metastore context, JavaEmitter emitter) {
        List<JavaFile> elementFiles = context.readAll(Element.class).stream()
                .map(element -> {
                    Identifier elementId = Identifier.from(element.getId()).orElseThrow();

                    // If the ElementType is not set, check if there's a manual override
                    if(element.getElementType() == null) {
                        ElementType elementType =
                                context.read(elementId, ElementType.class)
                                    .orElse(null);
                        element.setElementType(elementType);
                    }

                    // Filter out any we don't have types for
                    if(element.getElementType() != null) {
                        String pkg = JavaUtils.idToFQN(elementId).packageName();
                        TypeSpec java = emitter.handleElement(element);
                        return JavaFile.builder(pkg, java).build();
                    }

                    return null;
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());

        return elementFiles;
    }

    public static List<JavaFile> processDatasets(Metastore context, JavaEmitter emitter) {
        List<Dataset> datasets = context.readAll(Dataset.class);

        List<JavaFile> files = datasets.stream()
                .map(item -> {
                    String pkg = JavaUtils.idToClassName(item.getId().toString()).packageName();
                    TypeSpec java = emitter.handleDataset(item);
                    return JavaFile.builder(pkg, java).build();
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());

        return files;
    }

    public static void main(String[] args) {
        Metastore metastore = MetastoreCLIProcessor.getInputMetastore(args).orElseThrow();

        // Create a JavaEmitter with the FileStore
        JavaEmitter javaEmitter = new JavaEmitter(metastore);

        // Generate the JavaFiles
        List<JavaFile> javaFiles = JavaEmitter.process(metastore);

        // Write the JavaFiles to the output directory
        Path outputFolder = MetastoreCLIProcessor.getOutputPath(args)
                .map(path -> path.toAbsolutePath())
                .orElse(Paths.get("build").toAbsolutePath());
        System.out.println("JavaEmitter output folder: " + outputFolder);

        javaFiles.forEach(javaFile -> {
            try {
                javaFile.writeTo(outputFolder);
                System.out.println(("JavaEmitter wrote: " + javaFile.packageName + "." + javaFile.typeSpec.name));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }}
