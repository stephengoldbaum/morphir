package datathread.frontends;

import datathread.Identifier;
import datathread.JavaUtils;
import datathread.grammar.*;
import datathread.metastore.FileMetastore;
import datathread.metastore.Metastore;
import datathread.metastore.MetastoreFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static datathread.grammar.Elements.classToElementType;

/**
 * BasicJavaFrontend class for processing Java classes into Elements or Datasets
 * and storing them in a Metastore.
 */
public class BasicJavaFrontend {

    /**
     * Processes a given class and returns either an Element or a Dataset.
     *
     * @param clazz the class to be processed
     * @return an Object representing either an Element or a Dataset
     */
    public static Object processClass(Class clazz) {
        return isElement(clazz) ? processClassAsElement(clazz) : processClassAsDataset(clazz);
    }

    /**
     * Determines if a given class should be processed as an Element.
     *
     * @param clazz the class to be checked
     * @return true if the class should be processed as an Element, false otherwise
     */
    public static boolean isElement(Class clazz) {
        return clazz.isPrimitive() || clazz.isEnum() || clazz.getDeclaredFields().length <= 1;
    }

    /**
     * Processes a given class as a Dataset.
     *
     * @param clazz the class to be processed
     * @return a Dataset object representing the class
     */
    public static Dataset processClassAsDataset(Class clazz) {
        final String scheme = "dataset";

        // Convert class to Identifier
        Identifier id = JavaUtils.classToIdentifier(scheme, clazz);
        // Process fields of the class
        List<Field> fields = processFields(clazz);

        // Create and populate Dataset object
        Dataset dataset = new Dataset();
        dataset.setId(Identifier.toString(id));
        dataset.setName(JavaUtils.escapeFromJava(clazz.getSimpleName()));
        dataset.setFields(fields);

        return dataset;
    }

    /**
     * Processes a given class as an Element.
     *
     * @param clazz the class to be processed
     * @return an Element object representing the class
     */
    public static Element processClassAsElement(Class clazz) {
        final String scheme = "element";

        // Convert class to Identifier
        Identifier id = JavaUtils.classToIdentifier(scheme, clazz);

        // Create and populate Element object
        Element element = new Element();
        element.setId(Identifier.toString(id));
        element.setName(clazz.getSimpleName());

        // Process fields of the class
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 1) {
            Class fieldClass = fields[0].getType();
            ElementType elementType = classToElementType(fieldClass);
            element.setElementType(elementType);
        }

        return element;
    }

    /**
     * Processes the fields of a given class.
     *
     * @param clazz the class whose fields are to be processed
     * @return a list of Field objects representing the fields of the class
     */
    public static List<Field> processFields(Class clazz) {
        // Convert each field to a Field object
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .map(field -> {
                    Identifier id = JavaUtils.classToIdentifier("element", field.getType());
                    Field f = new Field();
                    f.setElement(Identifier.toString(id));
                    f.setName(field.getName());
                    return f;
                })
                .collect(Collectors.toList());

        return fields;
    }

    /**
     * Processes a list of classes and stores the resulting Elements or Datasets in the Metastore.
     *
     * @param classes the list of classes to be processed
     * @param metastore the Metastore to store the processed objects
     */
    public static void process(List<Class> classes, Metastore metastore) {
        classes.forEach(clazz -> {
            Object o = processClass(clazz);

            if (o instanceof Element) {
                Element element = (Element) o;
                metastore.write(Identifier.from(element.getId()).get(), element);
                System.out.println("BasicJavaFrontend.process: " + element.getId());
            } else if (o instanceof Dataset) {
                Dataset dataset = (Dataset) o;
                metastore.write(Identifier.from(dataset.getId()).get(), dataset);
                System.out.println("BasicJavaFrontend.process: " + dataset.getId());
            }
        });
    }

    /**
     * Main method to run the BasicJavaFrontend.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Get output path for Metastore
        Path output = MetastoreFactory.getOutputPath(args).orElse(Path.of("build", "metastore"));
        Metastore metastore = new FileMetastore(output);
        int start = 0;
        int end = args.length;

        // Parse command line arguments to find class names
        for(int i = 0; i < args.length; i++) {
            String current = args[i];

            // If already started
            if (start > 0) {
                // If it's a flag, end the index
                if (current.startsWith("--")) {
                    end = i;
                    break;
                }
            } else {
                // If it's a flag, skip it and the next argument
                if (current.startsWith("--")) {
                    i++;
                } else {
                    // Starts the index of the first class
                    start = i;
                }
            }
        }

        // Convert class names to Class objects
        List<Class> classes = Arrays.asList(args).subList(start, end).stream()
                .flatMap(BasicJavaFrontend::getClass)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Process the classes and store in Metastore
        process(classes, metastore);
    }

    /**
     * Retrieves a Class object for a given class name.
     *
     * @param className the name of the class to be retrieved
     * @return a Stream containing the Class object if found, or an empty Stream if not found
     */
    public static Stream<Class> getClass(String className) {
        try {
            Class c = Class.forName(className);
            return Optional.of(c).stream();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + className);
            return Stream.empty();
        }
    }
}