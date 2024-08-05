package datathread.metastore;

import datathread.Identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;


public class FederatedMetastore implements Metastore {
    private final List<Metastore> delegates;

    public FederatedMetastore(List<Metastore> delegates) {
        this.delegates = delegates == null ? Collections.emptyList() : delegates;
    }

    public <T> Optional<T> read(Identifier id, Class<T> tipe) {
        return this.delegates.stream()
                .flatMap(delegate -> delegate.read(id, tipe).stream())
                .findFirst();
    }

    public <T> List<T> readAll(Class<T> tipe) {
        return this.delegates.stream()
                .flatMap(delegate -> delegate.readAll(tipe).stream())
                .collect(Collectors.toList());
    }

    public <T> Optional<String> write(Identifier id, T data) {
        return Optional.of("Write is not supported for FederatedMetastore.");
    }
}
