/**
 * @fileoverview This file contains the implementation of a GraphQL resolvers.
 * @module graphql_resolver
 */
const { log } = require("console");


class ElementResolver {
    constructor(storage) {
        this.storage = storage;
    }

    element(id) {
      var element = this.storage.resolveAndRead(id, "element");

      if(!(element === undefined) && element !== null) {
        element = inflateElement(this.storage, element);
      }

      return element;
    }

    elements() {
      const elements =
        this.storage.findAllAndRead('element.json')
        .map(element => inflateElement(this.storage, element));

      return elements;
    }
}
 function inflateElement(storage, element) {
  const elementType = element.element_type;
    if (elementType && typeof elementType === 'object') {
        const typeNames = {
            'Number': 'NumberType',
            'Reference': 'ReferenceType',
            'Text': 'TextType',
            'Date': 'DateType',
            'Time': 'TimeType',
            'DateTime': 'DateTimeType',
            'Boolean': 'BooleanType',
            'Enum': 'EnumType'
        };

        for (const key in typeNames) {
            if (elementType.hasOwnProperty(key)) {
                elementType.__typename = typeNames[key];

                if (key === 'Reference') {
                    const refId = elementType.Reference.ref;
                    const referencedElement = storage.resolveAndRead(refId, "element");
                    elementType.Reference.ref = inflateElement(storage, referencedElement);
                }
                if (key === 'Boolean') {
                    elementType.Bool = {};
                }
                break;
            }
        }

        if (!element.info) {
          const infoOverride = storage.resolveAndRead(element.id, "element_info");
          element.info = infoOverride;
        }
    }

    return element;
}


class DatasetResolver {
    constructor(storage, elementResolver) {
        this.storage = storage;
        this.elementResolver = elementResolver;
    }

     dataset(id) {
      const dataset = this.storage.resolveAndRead(id, "dataset");
      return inflateDataset(dataset);
    }

    datasets() {
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
          const elmt = this.elementResolver.element(elementUrn);
          field.element = elmt;
        }
        else if (typeof field.element === 'string')
        {
          const elementUrn = field.element;
          const elmt = this.elementResolver.element(elementUrn);
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
          const elmt = this.elementResolver.element(elementUrn);
          field.element = elmt;
        }

        return field;
      });

      dataset.fields = fields;

      return dataset;
    }
}

//// Module ////
module.exports = {
    DatasetResolver,
    ElementResolver
};
