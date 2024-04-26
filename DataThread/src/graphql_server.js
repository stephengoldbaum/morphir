/**
 * @fileoverview This file contains the implementation of a GraphQL server using Express.
 * It defines the GraphQL schema, implements the resolvers, and creates an Express server with a GraphQL endpoint.
 * The server also serves the GraphiQL IDE and listens on port 4000.
 * Additionally, it includes helper functions for recursive search, converting URNs to file paths, and inflating datasets and elements.
 * @module graphql_server
 */
var express = require("express")
var { createHandler } = require("graphql-http/lib/use/express")
var { buildSchema } = require("graphql")
const { mergeTypeDefs } = require('@graphql-tools/merge');
const { makeExecutableSchema } = require("@graphql-tools/schema");
var { ruruHTML } = require("ruru/server")
const fs = require('fs');
const path = require('path');
const { log } = require("console");

module.exports = { runGraphQL };

var baseDir;
runGraphQL();

/**
 * Runs the GraphQL server.
 */
function runGraphQL() {
  // Setup
  const baseDirArg = process.argv.includes('--baseDir') 
  ? process.argv[process.argv.indexOf('--baseDir') + 1] 
  : 'metastore';

  baseDir = path.resolve(process.cwd(), baseDirArg);
  log("Using base folder: " + baseDir);

  // Define the GraphQL schema
  const elementSchemaFile = fs.readFileSync(path.join(path.join(__dirname, 'DataThread'), 'Element.schema.graphql'), 'utf8');
  const datasetSchemaFile = fs.readFileSync(path.join(path.join(__dirname, 'DataThread'), 'Dataset.schema.graphql'), 'utf8');
  const querySchemaFile = fs.readFileSync(path.join(__dirname, 'DataThread.schema.graphql'), 'utf8');
  // const schema = mergeTypeDefs([elementSchemaFile, datasetSchemaFile, querySchemaFile]);
  // const schema = buildSchema(schemaFile);

  //Implement the resolvers
  // const root = {
  //   dataset: ({ id }) => { return dataset(id); },
  //   datasets: () => { return datasets(); },
  //   element: ({ id }) => { return element(id); },
  //   elements: () => { datasets(); }, // TODO
  // };
  const resolvers = {
    Query: {
      dataset: ({ id }) => { return dataset(id); },
      datasets: () => { return datasets(); },
      element: ({ id }) => { return element(id); },
      elements: () => { datasets(); },
    }
  };

  const schema = makeExecutableSchema({
    typeDefs: [elementSchemaFile, datasetSchemaFile, querySchemaFile],
    resolvers: resolvers
  });
  
  // Create an express server and a GraphQL endpoint
  var app = express();

  // Create and use the GraphQL handler.
  app.all(
    "/graphql",
    createHandler({
      schema: schema,
      rootValue: resolvers
    })
  )


  // Serve the GraphiQL IDE.
  app.get("/", (_req, res) => {
    res.type("html")
    res.end(ruruHTML({ endpoint: "/graphql" }))
  })

  // Start the server at port
  app.listen(4000)
  console.log("Running a GraphQL API server at http://localhost:4000/graphql")
}

/**
 * Recursively searches a directory for files matching a specific pattern.
 * 
 * @param {string} dir - The directory to search.
 * @param {string} pattern - The file pattern to match.
 * @returns {string[]} - An array of file paths matching the pattern.
 */
function recursiveSearch(dir, pattern) {
  let results = [];

  fs.readdirSync(dir).forEach((file) => {
    const fullPath = path.resolve(dir, file);

    if (fs.statSync(fullPath).isDirectory()) {
      results = results.concat(recursiveSearch(fullPath, pattern));
    } else if (file.endsWith(pattern)) {
      results.push(fullPath);
    }
  });

  return results;
}

function urnToFile(urn) {
  return urnToFile(urn, undefined);
}

/**
 * Converts a URN (Uniform Resource Name) to a file path.
 * @param {string} urn - The URN to convert.
 * @param {string} [typ] - The file type extension. If not provided, it will be extracted from the URN.
 * @returns {string} The file path corresponding to the URN.
 */
function urnToFile(urn, typ) {
  const items = urn.split(':');

  if(typ === undefined || typ == null) {
    typ = items[0];
  }

  const file = path.join(baseDir, `/${items[1]}`, `${items[2]}.${typ}.json`)
  return file;
}

function dataset(id) {
  const dataset = getJSONData(id, "dataset");
  return inflateDataset(dataset);
}

function datasets() {
  var files = recursiveSearch(baseDir, 'dataset.json');

  const datasets = files.map(file => {
    const dataset = inflate(file);
    return inflateDataset(dataset);
  });
  return datasets;
}

/**
 * Inflates the dataset by resolving field overrides and inflating field elements.
 * @param {object} dataset - The dataset to be inflated.
 * @returns {object} - The inflated dataset.
 */
function inflateDataset(dataset) {
  const fields = dataset.fields.map(field => {
    const fieldUrn = `${dataset.id}#${field.name}`.replace("dataset:", ":");
    const fieldOverride = getJSONData("field" + fieldUrn, "field");

    if(fieldOverride !== undefined && fieldOverride !== null) {
      log("Found field override: " + field.id + " in " + dataset.id);
      field = fieldOverride;
    }

    if(field.element === undefined) 
    {
      const elementUrn = "element" + fieldUrn;
      const elmt = element(elementUrn);
      field.element = elmt;
    } 
    else if (typeof field.element === 'string') 
    {
      const elementUrn = field.element;
      const elmt = element(elementUrn);
      field.element = elmt;
    } 
    else 
    {
      const elmt = inflateElement(field.element);
      field.element = elmt;
    }
    
    if(field.element === undefined || field.element == null) {
      log("Setting " + field.name + " to nil in " + dataset.id);
      const elementUrn = "element:core:nil";
      const elmt = element(elementUrn);
      field.element = elmt;
    }

    return field;
  });

  dataset.fields = fields;

  return dataset;
}

function element(id) {
  var element = getJSONData(id, "element");

  if(!(element === undefined) && element !== null) {
    element = inflateElement(element);
  }

  return element;
}

function inflateElement(element) {
  const elementType = element.element_type;

  if (!(elementType === undefined) && typeof elementType === 'object') {
    if (elementType.hasOwnProperty('Number')) {
      elementType.__typename = 'NumberType';
    }
    else if(elementType.hasOwnProperty('Reference')) {
      elementType.__typename = 'ReferenceType';
      var refId = elementType.Reference.ref;
      var ref = inflateElement(getJSONData(refId, "element"));
      elementType.Reference.ref = ref;
    }
    else if (elementType.hasOwnProperty('Text')) {
      elementType.__typename = 'TextType';
    }
    else if (elementType.hasOwnProperty('Date')) {
      elementType.__typename = 'DateType';
    }
    else if (elementType.hasOwnProperty('Time')) {
      elementType.__typename = 'TimeType';
    }
    else if (elementType.hasOwnProperty('DateTime')) {
      elementType.__typename = 'DateTimeType';
    }
    else if (elementType.hasOwnProperty('Boolean')) {
      elementType.__typename = 'BooleanType';
      elementType.Bool = {};
    }
    else if (elementType.hasOwnProperty('Enum')) {
      elementType.__typename = 'EnumType';
    }

    const infoProperty = element.info;

    if(infoProperty === undefined) {
      const info = getJSONData(element.id, "element_info");
      element.info = info;
    }
  }

  return element;
}

function getJSONData(id) {
  return getJSONData(id);
}

function getJSONData(id, typ) {
  const file = urnToFile(id, typ);
  return inflate(file);
}

function inflate(file) {
  if (fs.existsSync(file)) {
    const data = fs.readFileSync(file);
    return JSON.parse(data);
  } else {
    return undefined;
  }
}
