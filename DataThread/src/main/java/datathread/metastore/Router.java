package datathread.metastore;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.text.translate.NumericEntityUnescaper.OPTION;

import java.util.Map;

import datathread.Indentifier;

public class Router implements Metastore {
    private final Map<Class, Metastore> routes;

    public Router(Map<Class, Metastore> routes) {
        this.routes = routes;
    }

    public <T> Optional<T> resolveAndRead(Indentifier id, Class<T> tipe) {
        Optional<Metastore> handler = Optional.of(this.routes.get(tipe));

        return handler
            .map(h -> h.resolveAndRead(id, tipe))
            .filter(o -> tipe.isInstance(o))
            .map(o -> tipe.cast(o))
            ;
    }

    public <T> List<T> findAllAndRead(Class<T> tipe) {
        Metastore handler = this.routes.get(tipe);

        if(handler != null) {
            return handler.findAllAndRead(tipe);
        }
        else {
            return Collections.emptyList();
        }
    }

    public <T> Optional<String> write(Indentifier id, Class<T> tipe, T data) {
        Metastore handler = this.routes.get(tipe);

        if(handler != null) {
            return handler.write(id, tipe, data);
        } else {
            return Optional.of("No storage handler for type " + tipe);
        }
    }
}
