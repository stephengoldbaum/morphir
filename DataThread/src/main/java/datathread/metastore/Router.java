package datathread.metastore;

import java.util.*;

import datathread.Identifier;

public class Router implements Metastore {
    private final Map<Class, Metastore> routes;

    public Router(Map<Class, Metastore> routes) {
        this.routes = routes;
    }

    public <T> Optional<T> resolveAndRead(Identifier id, Class<T> tipe) {
        return Optional.ofNullable(this.routes.get(tipe))
            .flatMap(h -> h.resolveAndRead(id, tipe))
            .filter(tipe::isInstance)
            .map(tipe::cast);
    }

    public <T> List<T> findAllAndRead(Class<T> tipe) {
        return Optional.ofNullable(this.routes.get(tipe))
            .map(h -> h.findAllAndRead(tipe))
            .orElse(Collections.emptyList());
    }

    public <T> Optional<String> write(Identifier id, Class<T> tipe, T data) {
        Metastore handler = this.routes.get(tipe);

        return (handler == null) 
            ? Optional.of("No storage handler for type " + tipe)
            : handler.write(id, tipe, data);
    }

    public static void main(String[] args) {
        final Class stringClass = String.class;

        final Metastore m1 = new Metastore() {
            public <T> Optional<T> resolveAndRead(Identifier id, Class<T> tipe) {
                return Optional.empty();
            }

            public <T> List<T> findAllAndRead(Class<T> tipe) {
                return Collections.emptyList();
            }

            public <T> Optional<String> write(Identifier id, Class<T> tipe, T data) {
                return tipe == stringClass ? Optional.empty() : Optional.of("No storage handler for type " + tipe);
            }
        };

        final Router router = new Router(Map.of(stringClass, m1));
        final String[] domain = {"domain1", "domain2"};
        final Identifier id = new Identifier("element", domain, "Name1");
        Object o = router.write(id, stringClass, "hello");
        System.out.println(o);

    }
}