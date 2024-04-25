# Data Sharing Contract Specification

## 1. Introduction

This specification defines a data sharing contract that allows for the definition of core metadata to define datasets and elements along with the ability to augment those with complementary sets of metadata into a loosely-coupled web of information.

## 2. Terminology

- **Element**: An individual piece of data that combines a business concept with strong data constraint information. It is uniquely identified by URI.
- **Dataset**: A collection of elements into a named structure. It is uniquely identified by URI.
- **URI**: [Uniform Resource Identifier](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier), an IETF standard for unique identifier used to identify datasets and elements.
- **Metadata**: Additional information about datasets and elements.
- **Metadata Set**: A collection of metadata organized into a contextual set.
- **JSON Schema**: JSON Schema is a vocabulary that allows you to annotate and validate JSON documents. It provides a way to describe the structure and constraints of JSON data. For more information, you can refer to the [JSON Schema specification](https://json-schema.org/).
- **JSON-LD**: [JSON-LD](https://json-ld.org/) is a lightweight Linked Data format that allows for the integration of data from different sources. It provides a way to express data using JSON and link it to external resources using URIs. For more information, you can refer to the [JSON-LD specification](https://json-ld.org/spec/latest/json-ld/).
- **GraphQL**: [GraphQL](https://graphql.org/) is a query language for APIs and a runtime for executing those queries with existing data. It provides a flexible and efficient way to request and manipulate graphs of data from multiple sources. For more information, you can refer to the [GraphQL specification](https://spec.graphql.org/).

## 3. Data Structures

### 3.1 Dataset
## Dataset Structure

The `Dataset` structure in Elm is used to represent a collection of data fields. Here's a breakdown of its fields:

- `id` (type: `DatasetID`): A unique identifier for the dataset. `DatasetID` is a type alias for `ID`.

- `name` (type: `String`): The name of the dataset.

- `version` (type: `Int`): The version of the dataset.

- `fields` (type: `List Field`): A list of fields in the dataset.

Each `Field` in the `fields` list is an object with the following structure:

- `name` (type: `FieldName`): The name of the field. `FieldName` is a type alias for `String`.

- `element` (type: `ElementID`): The unique identifier of the element associated with the field.

- `optional` (type: `Maybe Bool`): An optional boolean value indicating whether the field is optional. If this field is `Nothing`, it means the optionality of the field is not specified.

- `key` (type: `Maybe Bool`): An optional boolean value indicating whether the field is a key. If this field is `Nothing`, it means it is not specified whether the field is a key.

### 3.2 Element
The `Element` structure in Elm is used to represent a data element with a specific type and constraints. Here's a breakdown of its fields:

- `id` (type: `ElementID`): A unique identifier for the element. `ElementID` is a type alias for `ID`.

- `name` (type: `String`): The name of the element.

- `element_type` (type: `ElementType`): The type of the element. This can be one of several types:
  - `Text (Maybe TextConstraints)`: A text element, optionally with constraints on its length.
  - `Number (Maybe NumberConstraints)`: A numeric element, optionally with constraints on its minimum and maximum values and precision.
  - `Date`: A date element.
  - `Time`: A time element.
  - `DateTime`: A date-time element.
  - `Boolean`: A boolean element.
  - `Enum (List String)`: An enumerated element, with a list of possible values.
  - `Reference ElementID`: A reference to another element, identified by its `ElementID`.

The `TextConstraints` and `NumberConstraints` types define constraints for text and number elements, respectively:

- `TextConstraints` has two fields: `min_length` and `max_length`, both of which are optional and represent the minimum and maximum length of the text.

- `NumberConstraints` has three fields: `minimum`, `maximum`, and `precision`, all of which are optional. `minimum` and `maximum` represent the minimum and maximum value of the number, and `precision` represents the number of decimal places.

The `ElementInfo` structure provides additional information about an element:
- `id` (type: `ElementID`): The unique identifier of the element.
- `description` (type: `Maybe String`): An optional description of the element.
- `display_name` (type: `Maybe String`): An optional display name for the element.
- `short_display_name` (type: `Maybe String`): An optional short display name for the element.

## 4. Metadata
Metadata sets are defined in their own files with the naming convention of `[name].[metadata set].json`. They sit in a folder structured with a path matching the domain. Each set of metadata is defined with a combination of JSON Schema, JSON-LD, and GraphQL:

- **JSON Schema**: Used to define the structure of the metadata sets, providing a clear and understandable format for programmers.
- **JSON-LD**: Used to link disjoint properties from these metadata sets into an RDF graph, allowing for the integration of isolated sets of metadata.
- **GraphQL**: Used to unify the disjoint metadata sets into a queryable unified facade, providing a flexible and efficient way to query the metadata.

### 4.1 Element Metadata

Element metadata augments an element and is defined in a file named `[element name].element_info.json`. It sits in the folder alongside the element.

### 4.2 Dataset Metadata

Dataset metadata augments a dataset and is defined in a file named `[dataset name].dataset_info.json`. It sits in the folder alongside the dataset.

## 5. Operations
An API is provided to 

### 5.1 Create Dataset

To create a dataset, a JSON object with the properties defined in section 3.1 is added to the system.

### 5.2 Update Dataset

To update a dataset, the JSON object for the dataset is modified.

### 5.3 Delete Dataset

To delete a dataset, the JSON object for the dataset is removed from the system.

### 5.4 Create Element

To create an element, a JSON object with the properties defined in section 3.2 is added to a dataset.

### 5.5 Update Element

To update an element, the JSON object for the element is modified.

### 5.6 Delete Element

To delete an element, the JSON object for the element is removed from its dataset.

## Storage

### 

## 7. Error Handling

If an operation is attempted on a dataset or element that does not exist, an error is returned.

## 8. Conformance Criteria

An implementation is considered conformant if it correctly implements all of the operations defined in section 5 and correctly handles errors as defined in section 6.