package datathread;

import com.squareup.javapoet.ClassName;

import javax.validation.constraints.NotNull;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JavaUtils {

    //// Utils ////
    public static ClassName idToFQN(@NotNull Identifier id) {
        String pkg = Arrays.stream(id.domain())
                .map(JavaUtils::escapeToJava)
                .collect(Collectors.joining("."));
        String name = escapeToJava(id.name());

        return ClassName.get(pkg, name);
    }

    public static ClassName idToClassName(String id) {
        return idToFQN(Identifier.from(id).orElseThrow());
    }

    public static String escapeToJava(String name) {
        String decoded = URLDecoder.decode(name, StandardCharsets.UTF_8);
        String result = decoded
                .replaceAll("_", "__")
                .replaceAll("&", "_and_")
                .replaceAll(",", "_et_")
                .replaceAll(" ", "_")
                ;

        return result;
    }

    public static String escapeFromJava(String name) {
        String decoded = URLDecoder.decode(name, StandardCharsets.UTF_8);

        // Do not change the order
        String result = decoded
                .replaceAll("_and_", "&")
                .replaceAll("_et_", ",")
                .replaceAll("__", "|")
                .replaceAll("_", " ")
                .replaceAll("\\|", "_")
                ;

        return result;
    }

    public static String capitalize(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }


    public static Identifier classToIdentifier(String scheme, Class clazz) {
        final String name = JavaUtils.escapeFromJava(clazz.getSimpleName());
        final String[] domain = clazz.isPrimitive() ?
                new String[0] :
                Arrays.stream(clazz.getPackage().getName().split("\\.")).
                        map(JavaUtils::escapeFromJava).
                        toArray(String[]::new);

        return new Identifier(scheme, domain, name);
    }

}
