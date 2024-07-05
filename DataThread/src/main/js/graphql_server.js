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

const file_store = require('./file_store');
const gql_resolvers = require('./graphql_resolver');

module.exports = { runGraphQL };

runGraphQL();

/**
 * Runs the GraphQL server.
 */
function runGraphQL() {
  // Setup
  const baseDirArg = process.argv.includes('--baseDir') 
      ? process.argv[process.argv.indexOf('--baseDir') + 1]
      : 'metastore';

  const baseDir = path.resolve(process.cwd(), baseDirArg);
    log("Using base folder: " + baseDir);

  const automatedStorage = new file_store.Storage(path.resolve(baseDir, 'automated'));
  const editedStorage = new file_store.Storage(path.resolve(baseDir, 'edited'));

  const elementInfoResolver = new gql_resolvers.ElementInfoResolver(editedStorage);
  const typeResolver = new gql_resolvers.TypeResolver([automatedStorage, editedStorage]);
  const elementResolver = new gql_resolvers.ElementResolver(automatedStorage, elementInfoResolver);
  const datasetResolver = new gql_resolvers.DatasetResolver(automatedStorage, elementResolver);

  const resolvers = {
    Query: {
      dataset: (_, { id }) => datasetResolver.get(id),
      datasets: () => datasetResolver.getAll(),
      // element: (_, { id }) => elementResolver.get(id),
      element: (_, { id }) => {
        const element = elementResolver.get(id);
        return resolveElementType(element);
      },
      // elements: () => elementResolver.getAll(),
      elements: () => {
        const elements = elementResolver.getAll();
        return elements.map(element => {
          return resolveElementType(element);
        });
      },
      baseType: (_, { id }) => elementResolver.getBaseType(id),
    },
    ElementType: {
      __resolveType(obj, context, info){
        return gql_resolvers.resolveElementType(obj, context, info);
      },
    }
    
  };

  function resolveElementType(element) {
    const elementType = typeResolver.get(element.id);

    if(elementType) {
      element.element_type = elementType.element_type;
    }

    if(element.element_type) {
      element.lineage = elementResolver.getElementLineage(element);
    }
    
    return element;
  }

  // Define the GraphQL schema
    const pathToGrammar = path.join(__dirname, '..', 'resources');
    const elementSchemaFile = fs.readFileSync(path.join(pathToGrammar, 'Element.graphqls'), 'utf8');
    const elementInfoSchemaFile = fs.readFileSync(path.join(pathToGrammar, 'ElementInfo.graphqls'), 'utf8');
    const elementLineageFile = fs.readFileSync(path.join(pathToGrammar, 'ElementLineage.graphqls'), 'utf8');
    const datasetSchemaFile = fs.readFileSync(path.join(pathToGrammar, 'Dataset.graphqls'), 'utf8');
    const querySchemaFile = fs.readFileSync(path.join(pathToGrammar, 'DataThread.graphqls'), 'utf8');
    const typeSchemaFile = fs.readFileSync(path.join(pathToGrammar, 'Type.graphqls'), 'utf8');
    const allSchemaFile = fs.readFileSync(path.join(pathToGrammar, 'All.graphqls'), 'utf8');

  const schema = makeExecutableSchema({
    typeDefs: [elementSchemaFile, elementInfoSchemaFile, elementLineageFile, datasetSchemaFile, querySchemaFile, typeSchemaFile, allSchemaFile],
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
