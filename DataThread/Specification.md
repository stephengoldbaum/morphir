# Data Sharing Contract Specification

## Introduction

This specification defines a data sharing contract that allows for the definition of core metadata to define datasets and elements along with the ability to augment those with complementary sets of metadata into a loosely-coupled web of information.

## Terminology
Specification Terms:
- **Element**: An individual piece of data that combines a business concept with strong data constraint information. It is uniquely identified by URI. `CUSIP`, `Report Date`, and `Account ID` are examples of Elements.
- **Dataset**: A collection of elements into a named structure. It is uniquely identified by URI.
- **URI**: [Uniform Resource Identifier](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier), an IETF standard for unique identifier used to identify datasets and elements.
- **Metadata**: Additional information about datasets and elements.
- **Metadata Set**: A collection of metadata organized into a contextual set.
- **Domain**: The hierarchical domain and/or subdomain in which metadata is organized.
- **Package**: A collection of any combination of Dataset, Elements, and augmenting Metadata Sets that are versioned and shared as a unit.

Referenced Technologies:
- **JSON Schema**: JSON Schema is a vocabulary that allows you to annotate and validate JSON documents. It provides a way to describe the structure and constraints of JSON data. For more information, you can refer to the [JSON Schema specification](https://json-schema.org/).
- **JSON-LD**: [JSON-LD](https://json-ld.org/) is a lightweight Linked Data format that allows for the integration of data from different sources. It provides a way to express data using JSON and link it to external resources using URIs. For more information, you can refer to the [JSON-LD specification](https://json-ld.org/spec/latest/json-ld/).
- **GraphQL**: [GraphQL](https://graphql.org/) is a query language for APIs and a runtime for executing those queries with existing data. It provides a flexible and efficient way to request and manipulate graphs of data from multiple sources. For more information, you can refer to the [GraphQL specification](https://spec.graphql.org/).

## Objectives
The main objective of this specification is to define a standard for sharing semantically-typed datasets across systems.  

The core requirements are:
- **Core Datasets** - Defining the minimum core data structures for Elements and Datasets.
- **Inclusive Authoring** - Support authoring Datasets and Elements through existing and future languages and technologies.
- **Machine processable** - Support processing shared Datasets and Elements across a variety of existing and future languages and technologies.
- **Extensible Metadata** - Allow arbitrary augmentation with new Metadata Sets at any time or location.

## Specification
The Specification takes the view that well-defined structures combined with flexible graphs provides an optimal balance for managing metdata. To achieve this, it defines metdata in a combination of `JSON Schema`, `JSON-LD`, and `GraphQL`. This gives developers a concrete specification to work with, while also allowing teams to easily augment the existing structures with Metadata Sets of their own.

The specification defines core Metadata Sets for Dataset and Element. All other Metadata Sets are managed outside of the core.

Important components of the Specification are:



### Identifiers
Every Dataset and Element must be uniquely identified. It is the responsibility of the owner to set the identification. To avoid clashes, the Specification follows the [Uniform Resource Identifier](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier) standard with the following URN structure of:
  - `[type]:[/domain][/subdomain?]:[name]`.

### Core Data Structures
#### Dataset Structure

The `Dataset` structure in Elm is used to represent a collection of data fields. Here's a breakdown of its fields:

| Field| Description |
|----|----|
| `id` | A unique identifier for the dataset. `DatasetID` is a type alias for `ID`. |
| `name` | The name of the dataset. |
| `version`| The version of the dataset. |
| `fields` | A list of fields in the dataset. |

Each `Field` in the `fields` list is an object with the following structure:

| Field| Description |
|----|----|
| `name` | The name of the field. `FieldName` is a type alias for `String`. |
| `element`| The unique identifier of the element associated with the field.|
| `optional` | An optional boolean value indicating whether the field is optional.|
| `key`| An optional boolean value indicating whether the field is a key. |

#### Element Structure
The `Element` structure in Elm is used to represent a data element with a specific type and constraints. Here's a breakdown of its fields:

| Property | Type | Description |
|----||--|
| `id` | `ElementID` | A unique identifier for the element. `ElementID` is a type alias for `ID`. |
| `name` | `String`| The name of the element.  |
| `element_type` | `ElementType` | The type of the element. This can be one of several types |

#### Element Type Structure
| Element Type | Constraints |
|---|--|
| `Text` (Maybe TextConstraints) | A text element, optionally with constraints on its length. |
| `Number` (Maybe NumberConstraints) | A numeric element, optionally with constraints on its minimum and maximum values and precision. |
| `Date` | A date element.|
| `Time` | A time element.|
| `DateTime` | A date-time element. |
| `Boolean` | A boolean element. |
| `Enum` | An enumerated element, with a list of possible values. |
| `Reference` | A reference to another element, identified by its `ElementID`. |

The  and `NumberConstraints` types define constraints for text and number elements, respectively:

##### `TextConstraints`
| Field | Type |Description |
|--|--|---|
| `min_length` | `Maybe Int` | An optional minimum length of the text. |
| `max_length` | `Maybe Int` | An optional maximum length of the text. |

##### `NumberConstraints`
| Field | Type | Description |
|--|--|--|
| `minimum`| `Maybe Float` | An optional minimum value of the number. |
| `maximum`| `Maybe Float` | An optional maximum value of the number. |
| `precision`| `Maybe Int` | An optional number of decimal places.|

##### `ElementInfo`
The `ElementInfo` structure provides additional information about an element:
| Property | Type | Description |
|--|--|--|
| `id`| `ElementID` | The unique identifier of the element. |
| `description` | `Maybe String`| An optional description of the element. |
| `display_name`| `Maybe String`| An optional display name for the element. |
| `short_display_name`| `Maybe String`| An optional short display name for the element. |

### Metadata Augmentation
Datasets and Elements can be augmented with new Metadata Sets at any time or place by linking the augmenting to the augmented through the augmented's Identifier URI. The Specification requires that MetadataSets define all of:
- **JSON Schema**: Used to define the structure of the metadata sets, providing a clear and understandable format for programmers.
- **JSON-LD**: Used to link disjoint properties from these metadata sets into an RDF graph, allowing for the integration of isolated sets of metadata.
- **GraphQL**: Used to unify the disjoint metadata sets into a queryable unified facade, providing a flexible and efficient way to query the metadata.

The core specification provides the following two basic augmenting Metadata Sets:

#### Element Metadata
The `ElementInfo` structure provides additional information about an element:

| Property | Type | Description |
|----|----|----|
| `id` | `ElementID` | The unique identifier of the element. |
| `description` | `Maybe String` | An optional description of the element. |
| `display_name` | `Maybe String` | An optional display name for the element. |
| `short_display_name` | `Maybe String` | An optional short display name for the element. |

Element metadata augments an element and is defined in a file named `[element name].element_info.json`. It sits in the folder alongside the element.

#### Dataset Metadata
TBD

### API
The Specification provides an API for interacting with a Package. The API should be used in order to abstract the underlying storage and publishing mechanism. This is necessary to insulate dependent tooling from structural changes.

#### Operations
##### Create Element
This type alias defines the structure of a `CreateElement` request, which includes the following fields:

###### `Create Element` Command
| Propery | Type | Description |
|--|--|--|
| `requestId` | URI | The ID of the request |
| `domain` | Domain | The domain of the element |
| `element` | Element | The element to be created |

###### `Element Created` Event
| Propery | Type | Description |
|--|--|--|
| `requestId` | URI | The ID of the request |
| `domain` | Domain | The domain of the element |
| `element` | Element | The element to be created |

##### Create Dataset
TBD 

##### `Request Failed` Event
| Propery | Type | Description |
|--|--|--|
| `requestId` | URI | The ID of the request |
| `reason` | String | The error cause |

#### Query
The Specification provides a standard mechanism to query the graph of structures associated with Datasets and Elements. The default implementation uses `GraphQL` for this purpose.

### Package & Publish
#### Packaging
The Specification defines a default mechanism for packaging and sharing Packages using a filesystem layout.

- **Domains** - Each domain is represented by a folder with the same name.
- **Metadata Sets** - Each MetadataSet (including the core) are defined in its own file with the naming convention of:

  **`[name].[metadata set type].json`**. 

- **Dependency Manifest** - Packages may depend on other Packages. This is declared in the Package manifest. Depedencies must be registered according to the Package Universal Resource Locator (purl) specification.

#### Publishing
The Specification allows publishing through any packaging and publishing standard that allows the required folder structure and manifest along with an accompanying service and/or API to query the contents.

The default implementation is `npm`.

### Integrations
#### Authoring (Frontends)
There are many existing technologies for authoring metadata models. The Specification does not mandate any particular technology. Rather it requires only that the chosen method provide the necessary JSON Schema, JSON-LD, and GraphQL structures. A small sampling of options include:
- TypeSpec (Microsoft)
- LinkML
- Taxi
- Morphir (FINOS)
- Legend (FINOS)
- Honeycomb (Morgan Stanley)
- Java with annotations (Morgan Stanley)
- Typescript 
- Smithy (AWS)
