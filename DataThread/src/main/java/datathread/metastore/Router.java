package datathread.metastore;

import datathread.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Router implements Metastore {
    private final Map<Class, Metastore> routes;

    public Router(Map<Class, Metastore> routes) {
        this.routes = routes;
    }

    public <T> Optional<T> read(Identifier id, Class<T> tipe) {
        return Optional.ofNullable(this.routes.get(tipe))
            .flatMap(h -> h.read(id, tipe))
            .filter(tipe::isInstance)
            .map(tipe::cast);
    }

    public <T> List<T> readAll(Class<T> tipe) {
        return Optional.ofNullable(this.routes.get(tipe))
            .map(h -> h.readAll(tipe))
            .orElse(Collections.emptyList());
    }

    public <T> Optional<String> write(Identifier id, T data) {
        Class tipe = data.getClass();
        Metastore handler = this.routes.get(tipe);

        return (handler == null) 
            ? Optional.of("No storage handler for type " + tipe)
            : handler.write(id, data);
    }

    @Override
    public Optional<String> delete(Identifier id) {
        String result = this.routes.values().stream()
                .map(delegate -> delegate.delete(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    public Optional<Metastore> getRoute(Class tipe) {
        return Optional.ofNullable(this.routes.get(tipe));
    }
}