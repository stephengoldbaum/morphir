/**
 * @fileoverview This file contains the implementation of a GraphQL resolvers.
 * @module graphql_resolver
 */
const { log } = require("console");

class ElementResolver {
  constructor(storage, elementInfoResolver) {
    this.storage = storage;
    this.elementInfoResolver = elementInfoResolver;
  }

  get(id) {
    let element = this.storage.resolveAndRead(id, "element");

    if (element !== undefined && element !== null) {
      element = this.inflateElement(element);
    }

    return element;
  }

  getAll() {
    const elements = this.storage.findAllAndRead('element.json')
      .map(element => this.inflateElement(element));

    return elements;
  }

  inflateElement(element) {
    const elementType = element.element_type;

    if (!elementType) {
      //TODO Look for type override in edited

    }

    if (elementType && typeof elementType === 'object') {
      const typeNames = {
        'Number': 'NumberType',
        'Reference': 'ReferenceType',
        'Text': 'TextType',
        'Date': 'DateType',
        'Time': 'TimeType',
        'DateTime': 'DateTimeType',
        'Boolean': 'BooleanType',
        'Enum': 'EnumType',
        'Record': 'RecordType'
      };

      for (const key in typeNames) {
        if (elementType.hasOwnProperty(key)) {
          elementType.__typename = typeNames[key];

          if (key === 'Reference') {
            const refId = elementType.Reference.ref;
            const referencedElement = this.storage.resolveAndRead(refId, "element");
            elementType.Reference.ref = this.inflateElement(referencedElement);
          }
          if (key === 'Boolean') {
            elementType.Bool = {};
          }
          break;
        }
      }

      if (!element.info) {
        const infoOverride = this.elementInfoResolver.get(element.id);

        if (infoOverride != undefined) {
          element.info = infoOverride;
        }
      }
    }

    return element;
  }

  findBaseType(id) {
    const element = this.get(id);
    return element ? getBaseType(element) : null;
  }

  getBaseType(element) {
    var elementType = element.element_type;

    while (elementType && element.Reference) {
      const refId = elementType.Reference.ref.id;
      const referencedElement = this.get(refId);
      elementType = referencedElement.element_type;
    }

    return elementType;
  }

  getElementLineage(element) {
    const lineage = [];
    var elementType = element.element_type;

    while (elementType && elementType.Reference) {
      lineage.push(elementType);

      const referencedElement = this.get(elementType.Reference.ref);

      elementType = referencedElement ? referencedElement.element_type : null;
    }

    if (elementType) {
      lineage.push(elementType);
    }

    return lineage;
  }
}

class DatasetResolver {
  constructor(storage, elementResolver) {
    this.storage = storage;
    this.elementResolver = elementResolver;
  }

  get(id) {
    const dataset = this.storage.resolveAndRead(id, "dataset");
    return inflateDataset(dataset);
  }

  getAll() {
    const datasets = this.storage
      .findAllAndRead('dataset.json')
      .map(dataset => this.inflateDataset(dataset));

    return datasets;
  }

  /**
   * Inflates the dataset by resolving field overrides and inflating field elements.
   * @param {object} dataset - The dataset to be inflated.
   * @returns {object} - The inflated dataset.
   */
  inflateDataset(dataset) {
    const fields = dataset.fields.map(field => {
      const fieldUrn = `${dataset.id}#${field.name}`.replace("dataset:", ":");
      const fieldOverride = this.storage.resolveAndRead("field" + fieldUrn, "field");

      if (fieldOverride !== undefined && fieldOverride !== null) {
        log("Found field override: " + field.id + " in " + dataset.id);
        field = fieldOverride;
      }

      if (field.element === undefined) {
        const elementUrn = "element" + fieldUrn;
        const elmt = this.elementResolver.get(elementUrn);
        field.element = elmt;
      }
      else if (typeof field.element === 'string') {
        const elementUrn = field.element;
        const elmt = this.elementResolver.get(elementUrn);
        field.element = elmt;
      }
      else {
        const elmt = this.elementResolver.inflateElement(field.element);
        field.element = elmt;
      }

      if (field.element === undefined || field.element == null) {
        log("Setting " + field.name + " to nil in " + dataset.id);
        const elementUrn = "element:core:nil";
        const elmt = this.elementResolver.get(elementUrn);
        field.element = elmt;
      }

      return field;
    });

    dataset.fields = fields;

    return dataset;
  }
}

class ElementInfoResolver {
  constructor(storage) {
    this.storage = storage;
  }

  get(id) {
    var item = this.storage.resolveAndRead(id, "element_info");
    return item;
  }
}

class TypeResolver {
  constructor(storages) {
    this.storages = storages;
  }

  get(id) {
    var item = null;

    // Reverse order to let override take precendence
    for (let index = this.storages.length - 1; index >= 0 && !item; index--) {
      const storage = this.storages[index];
      item = storage.resolveAndRead(id, "element");
    }

    return item ? item.element_type : null;
  }
}

/**
 * Resolves the element type based on the provided object.
 * @param {object} obj - The object to resolve the element type from.
 * @param {object} context - The context object.
 * @param {object} info - The info object.
 * @returns {string|null} - The resolved element type or null if not found.
 */
function resolveElementType(obj, context, info) {
  if (obj.Boolean) {
    return 'BooleanType';
  }

  if (obj.Date) {
    return 'DateType';
  }

  if (obj.DateTime) {
    return 'DateTimeType';
  }

  if (obj.Enum) {
    return 'EnumType';
  }

  if (obj.Number) {
    return 'NumberType';
  }

  if (obj.Record) {
    return 'RecordType';
  }

  if (obj.Reference) {
    return 'ReferenceType';
  }

  if (obj.Text) {
    return 'TextType';
  }

  if (obj.Time) {
    return 'TimeType';
  }

  return null;
}

//// Module ////
module.exports = {
  ElementResolver,
  DatasetResolver,
  ElementInfoResolver,
  TypeResolver,
  resolveElementType
};
