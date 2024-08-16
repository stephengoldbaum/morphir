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

public class BasicJavaFrontend {
    public static Object processClass(Class clazz) {
        return isElement(clazz) ? processClassAsElement(clazz) : processClassAsDataset(clazz);
    }

    public static boolean isElement(Class clazz) {
        return clazz.isPrimitive() || clazz.isEnum() || clazz.getDeclaredFields().length <= 1;
    }

    public static Dataset processClassAsDataset(Class clazz) {
        final String scheme = "dataset";

        Identifier id = JavaUtils.classToIdentifier(scheme, clazz);
        List<Field> fields = processFields(clazz);

        Dataset dataset = new Dataset();
        dataset.setId(Identifier.toString(id));
        dataset.setName(JavaUtils.escapeFromJava(clazz.getSimpleName()));
        dataset.setFields(fields);

        return dataset;
    }

    public static Element processClassAsElement(Class clazz) {
        final String scheme = "element";

        Identifier id = JavaUtils.classToIdentifier(scheme, clazz);

        Element element = new Element();
        element.setId(Identifier.toString(id));
        element.setName(clazz.getSimpleName());

        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 1) {
            Class fieldClass = fields[0].getType();
            ElementType elementType = classToElementType(fieldClass);
            element.setElementType(elementType);
        }

        return element;
    }

    public static List<Field> processFields(Class clazz) {
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

    public static void main(String[] args) {
        Path output = MetastoreFactory.getOutputPath(args).orElse(Path.of("build", "metastore"));
        Metastore metastore = new FileMetastore(output);
        int start = 0;
        int end = args.length;

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

        List<Class> classes = Arrays.asList(args).subList(start, end).stream()
                .flatMap(BasicJavaFrontend::getClass)
                .filter(clazz -> clazz != null)
                .collect(Collectors.toList());

        process(classes, metastore);
    }

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
