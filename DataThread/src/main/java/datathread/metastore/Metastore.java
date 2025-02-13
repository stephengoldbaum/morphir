package datathread.metastore;

import datathread.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Metastore operations.
 * Provides methods to read, write, and delete objects in the Metastore.
 */
public interface Metastore {
    /**
     * Reads an object of the specified type by its Identifier.
     *
     * @param id the Identifier of the object to be read
     * @param tipe the class type of the object to be read
     * @param <T> the type of the object to be read
     * @return an Optional containing the object if found, or an empty Optional if not found
     */
    public <T> Optional<T> read(Identifier id, Class<T> tipe);

    /**
     * Reads all objects of the specified type.
     *
     * @param tipe the class type of the objects to be read
     * @param <T> the type of the objects to be read
     * @return a list of all objects found
     */
    public <T> List<T> readAll(Class<T> tipe);

    /**
     * Writes an object to the Metastore.
     *
     * @param id the Identifier of the object to be written
     * @param data the object to be written
     * @param <T> the type of the object to be written
     * @return an Optional containing a message indicating the result of the write operation
     */
    public <T> Optional<String> write(Identifier id, T data);

    /**
     * Deletes an object by its Identifier.
     *
     * @param id the Identifier of the object to be deleted
     * @return an Optional containing the result of the delete operation, or an empty Optional if no deletion occurred
     */
    public Optional<String> delete(Identifier id);
}