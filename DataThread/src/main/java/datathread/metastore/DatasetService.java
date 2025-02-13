package datathread.metastore;

import datathread.Identifier;
import datathread.grammar.Dataset;

import java.util.Optional;

/**
 * Service class for managing Datasets in the Metastore.
 *
 * This class exists to provide a higher-level abstraction for interacting with Datasets stored in the Metastore.
 * It encapsulates the logic for retrieving and managing Datasets, making it easier for developers to work with
 * the underlying data storage without needing to understand the details of the Metastore implementation.
 *
 * Developers would use this class to:
 * 1. Retrieve Datasets by their Identifier.
 * 2. Simplify interactions with the Metastore by providing a focused API for Dataset-related operations.
 */
public class DatasetService {
    /**
     * Metastore instance to interact with the data storage.
     */
    private final Metastore metastore;

    /**
     * Constructor to initialize the DatasetService with a Metastore instance.
     *
     * @param metastore the Metastore instance to be used by this service
     */
    public DatasetService(Metastore metastore) {
        this.metastore = metastore;
    }

    /**
     * Method to retrieve a Dataset by its Identifier.
     *
     * @param id the Identifier of the Dataset to be retrieved
     * @return an Optional containing the Dataset if found, or an empty Optional if not found
     */
    public Optional<Dataset> get(Identifier id) {
        // Read the Dataset from the Metastore
        Dataset dataset = metastore.read(id, Dataset.class)
                .orElse(null);

        // Return the Dataset wrapped in an Optional
        return Optional.ofNullable(dataset);
    }
}