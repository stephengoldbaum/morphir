const path = require('path');
const requireDirectory = require('require-directory');


// Load all modules in the src/main/js directory
const modules = requireDirectory(module, path.join(__dirname, 'src/main/js'));

// Re-export the modules
module.exports = {
  file_store: modules.file_store,
  graphql_resolver: modules.graphql_resolver,
  graphql_server: modules.graphql_server,
  request_processor: modules.request_processor
};
module.exports = modules;
