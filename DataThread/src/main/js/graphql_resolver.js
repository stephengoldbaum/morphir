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

    if(element !== undefined && element !== null) {
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

    if(!elementType) {
      //TODO Look for type override in edited
    }
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

        if(fieldOverride !== undefined && fieldOverride !== null) {
          log("Found field override: " + field.id + " in " + dataset.id);
          field = fieldOverride;
        }

        if(field.element === undefined)
        {
          const elementUrn = "element" + fieldUrn;
          const elmt = this.elementResolver.get(elementUrn);
          field.element = elmt;
        }
        else if (typeof field.element === 'string')
        {
          const elementUrn = field.element;
          const elmt = this.elementResolver.get(elementUrn);
          field.element = elmt;
        }
        else
        {
          const elmt = this.elementResolver.inflateElement(field.element);
          field.element = elmt;
        }

        if(field.element === undefined || field.element == null) {
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
