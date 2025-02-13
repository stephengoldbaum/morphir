package datathread.metastore;

import datathread.Identifier;
import datathread.grammar.Element;
import datathread.grammar.ElementType;

import java.util.Optional;

/**
 * Service class for managing Elements in the Metastore.
 *
 * This class exists to provide a higher-level abstraction for interacting with Elements stored in the Metastore.
 * It encapsulates the logic for retrieving and managing Elements, making it easier for developers to work with
 * the underlying data storage without needing to understand the details of the Metastore implementation.
 *
 * Developers would use this class to:
 * 1. Retrieve Elements by their Identifier.
 * 2. Ensure that the ElementType is correctly set on retrieved Elements.
 * 3. Simplify interactions with the Metastore by providing a focused API for Element-related operations.
 */
public class ElementService {
    /**
     * Metastore instance to interact with the data storage.
     */
    private final Metastore metastore;

    /**
     * Constructor to initialize the ElementService with a Metastore instance.
     *
     * @param metastore the Metastore instance to be used by this service
     */
    public ElementService(Metastore metastore) {
        this.metastore = metastore;
    }

    /**
     * Method to retrieve an Element by its Identifier.
     *
     * @param id the Identifier of the Element to be retrieved
     * @return an Optional containing the Element if found, or an empty Optional if not found
     */
    public Optional<Element> get(Identifier id) {
        // Read the Element from the Metastore
        Element element = metastore.read(id, Element.class)
                .orElse(null);

        // If the Element is found but its ElementType is null, attempt to retrieve and set the ElementType
        if (element != null && element.getElementType() == null) {
            Optional<ElementType> oet = getElementType(id);

            // If the ElementType is found, set it on the Element
            oet.ifPresent(element::setElementType);
        }

        // Return the Element wrapped in an Optional
        return Optional.ofNullable(element);
    }

    /**
     * Method to retrieve an ElementType by its Identifier.
     *
     * @param id the Identifier of the ElementType to be retrieved
     * @return an Optional containing the ElementType if found, or an empty Optional if not found
     */
    Optional<ElementType> getElementType(Identifier id) {
        // Read the ElementType from the Metastore
        return metastore.read(id, ElementType.class);
    }
}