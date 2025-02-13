# Metastore Package Documentation

## Overview

The `metastore` package provides a set of classes and interfaces for managing and interacting with a metastore. The metastore is used to store, retrieve, and delete various types of data objects identified by unique identifiers.

## Classes and Interfaces

### `Metastore`

The `Metastore` interface defines the basic operations for interacting with the metastore.

#### Methods

- `<T> Optional<T> read(Identifier id, Class<T> tipe)`: Reads an object of the specified type by its Identifier.
- `<T> List<T> readAll(Class<T> tipe)`: Reads all objects of the specified type.
- `<T> Optional<String> write(Identifier id, T data)`: Writes an object to the metastore.
- `Optional<String> delete(Identifier id)`: Deletes an object by its Identifier.

### `FileMetastore`

The `FileMetastore` class is an implementation of the `Metastore` interface that uses the file system to store data objects.

#### Constructor

- `FileMetastore(Path baseDir, ObjectMapper objectMapper)`: Initializes the `FileMetastore` with a base directory and an optional `ObjectMapper`.
- `FileMetastore(Path baseDir)`: Initializes the `FileMetastore` with a base directory and a default `ObjectMapper`.

#### Methods

- `<T> Optional<T> read(Identifier id, Class<T> tipe)`: Reads an object of the specified type by its Identifier.
- `<T> List<T> readAll(Class<T> metaType)`: Reads all objects of the specified type.
- `Optional<String> delete(Identifier id)`: Deletes an object by its Identifier.
- `<T> Optional<String> write(Identifier id, T data)`: Writes an object to the metastore.

#### Static Helpers

- `static String classNameToFileStyle(Class tipe)`: Converts a class name to a file style string.
- `static String toFileStyle(String s)`: Converts a string to a file style string.
- `static String escape(String str)`: Escapes special characters in a string.
- `static String unescape(String str)`: Unescapes special characters in a string.
- `static Path resolveForID(Path baseDir, Identifier id, Class tipe)`: Resolves a file path for a given Identifier and type.
- `static Path resolveFile(Path baseDir, String schema, String[] domain, String name, String fileSuffix)`: Resolves a file path for given parameters.

### `DatasetService`

The `DatasetService` class provides methods to manage `Dataset` objects in the metastore.

#### Constructor

- `DatasetService(Metastore metastore)`: Initializes the `DatasetService` with a `Metastore` instance.

#### Methods

- `Optional<Dataset> get(Identifier id)`: Retrieves a `Dataset` by its Identifier.

### `ElementService`

The `ElementService` class provides methods to manage `Element` objects in the metastore.

#### Constructor

- `ElementService(Metastore metastore)`: Initializes the `ElementService` with a `Metastore` instance.

#### Methods

- `Optional<Element> get(Identifier id)`: Retrieves an `Element` by its Identifier.
- `Optional<ElementType> getElementType(Identifier id)`: Retrieves an `ElementType` by its Identifier.

## Usage

### Example Usage of `FileMetastore`

```java
import datathread.Identifier;
import datathread.metastore.FileMetastore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Optional;

public class Example {
    public static void main(String[] args) {
        Path baseDir = Path.of("/path/to/metastore");
        ObjectMapper objectMapper = new ObjectMapper();
        FileMetastore metastore = new FileMetastore(baseDir, objectMapper);

        Identifier id = new Identifier("scheme", new String[]{"domain"}, "name");
        Optional<MyDataClass> data = metastore.read(id, MyDataClass.class);

        data.ifPresent(d -> System.out.println("Data: " + d));
    }
}
```

### Example Usage of `DatasetService`

```java
import datathread.Identifier;
import datathread.metastore.DatasetService;
import datathread.metastore.FileMetastore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Optional;

public class Example {
    public static void main(String[] args) {
        Path baseDir = Path.of("/path/to/metastore");
        ObjectMapper objectMapper = new ObjectMapper();
        FileMetastore fileMetastore = new FileMetastore(baseDir, objectMapper);
        DatasetService datasetService = new DatasetService(fileMetastore);

        Identifier id = new Identifier("scheme", new String[]{"domain"}, "name");
        Optional<Dataset> dataset = datasetService.get(id);

        dataset.ifPresent(d -> System.out.println("Dataset: " + d));
    }
}
```

### Example Usage of `ElementService`

```java
import datathread.Identifier;
import datathread.metastore.ElementService;
import datathread.metastore.FileMetastore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Optional;

public class Example {
    public static void main(String[] args) {
        Path baseDir = Path.of("/path/to/metastore");
        ObjectMapper objectMapper = new ObjectMapper();
        FileMetastore fileMetastore = new FileMetastore(baseDir, objectMapper);
        ElementService elementService = new ElementService(fileMetastore);

        Identifier id = new Identifier("scheme", new String[]{"domain"}, "name");
        Optional<Element> element = elementService.get(id);

        element.ifPresent(e -> System.out.println("Element: " + e));
    }
}
```

## Testing

### Running Tests

To run the tests for the `FileMetastore` class, navigate to the directory containing the test file `test_file_metastore.py` and run the following command:

```sh
python -m unittest test_file_metastore.py
```

Ensure that the `unittest` framework is available, as it is included in the Python standard library.