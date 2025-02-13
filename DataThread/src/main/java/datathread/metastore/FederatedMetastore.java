package datathread.metastore;

import datathread.Identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FederatedMetastore class that delegates operations to multiple Metastore instances.
 *
 * Use this class in scenarios where they need to aggregate data from multiple metastore instances.
 *
 * Benefits of using FederatedMetastore:
 *
 * 1. **Data Aggregation**: It allows reading data from multiple metastore instances, providing a unified view of the data.
 * 2. **Redundancy and Failover**: By delegating operations to multiple metastore instances, redundancy is achieved. If one metastore fails, others can still provide the required data.
 * 3. **Scalability**: Distributing data across multiple metastore instances helps in scaling the system, as the load is shared among the instances.
 * 4. **Separation of Concerns**: Different metastore instances can be used for different types of data or different parts of the application, and FederatedMetastore provides a single interface to access all of them.
 * 5. **Flexibility**: It allows adding or removing metastore instances without changing the client code that interacts with the metastore.
 */
public class FederatedMetastore implements Metastore {
    /**
     * List of Metastore delegates.
     */
    private final List<Metastore> delegates;

    /**
     * Constructor to initialize the FederatedMetastore with a list of delegates.
     *
     * @param delegates the list of Metastore instances to delegate operations to
     */
    public FederatedMetastore(List<Metastore> delegates) {
        this.delegates = delegates == null ? Collections.emptyList() : delegates;
    }

    /**
     * Reads an object of the specified type by its Identifier from the first available delegate.
     *
     * @param id the Identifier of the object to be read
     * @param tipe the class type of the object to be read
     * @param <T> the type of the object to be read
     * @return an Optional containing the object if found, or an empty Optional if not found
     */
    public <T> Optional<T> read(Identifier id, Class<T> tipe) {
        // Stream through delegates and find the first non-empty result
        return this.delegates.stream()
                .flatMap(delegate -> delegate.read(id, tipe).stream())
                .findFirst();
    }

    /**
     * Reads all objects of the specified type from all delegates.
     *
     * @param tipe the class type of the objects to be read
     * @param <T> the type of the objects to be read
     * @return a list of all objects found
     */
    public <T> List<T> readAll(Class<T> tipe) {
        // Stream through delegates and collect all results into a list
        return this.delegates.stream()
                .flatMap(delegate -> delegate.readAll(tipe).stream())
                .collect(Collectors.toList());
    }

    /**
     * Writes an object to the Metastore. This operation is not supported for FederatedMetastore.
     *
     * @param id the Identifier of the object to be written
     * @param data the object to be written
     * @param <T> the type of the object to be written
     * @return an Optional containing a message indicating that the operation is not supported
     */
    public <T> Optional<String> write(Identifier id, T data) {
        return Optional.of("Write is not supported for FederatedMetastore.");
    }

    /**
     * Deletes an object by its Identifier from all delegates.
     *
     * @param id the Identifier of the object to be deleted
     * @return an Optional containing the result of the delete operations, or an empty Optional if no deletions occurred
     */
    @Override
    public Optional<String> delete(Identifier id) {
        // Stream through delegates and collect non-empty results into a single string
        String result = this.delegates.stream()
                .map(delegate -> delegate.delete(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    /**
     * Creates a new instance of FederatedMetastore with the provided list of delegates.
     *
     * @param delegates the list of Metastore instances to delegate operations to
     * @return a new instance of FederatedMetastore
     */
    public static FederatedMetastore newInstance(List<Metastore> delegates) {
        return new FederatedMetastore(delegates);
    }
}