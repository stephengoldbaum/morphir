const express = require('express');
const fs = require('fs');
const path = require('path');
const app = express();
const bodyParser = require('body-parser');
const { log } = require('console');
const file_store = require('./file_store');

const baseDirArg = process.argv.includes('--baseDir') 
  ? process.argv[process.argv.indexOf('--baseDir') + 1] 
  : 'metastore';

const baseDir = path.resolve(process.cwd(), baseDirArg);

if(!fs.existsSync(baseDir)) {
  fs.mkdirSync(baseDir);
}
console.log("Using base folder: " + baseDir);

const storage = new file_store.Storage(baseDir);

app.use(bodyParser.json());

app.post('/element', (req, res) => {
  const body = req.body;
  processRequest(body, 'element', req, res);
});

app.post('/dataset', (req, res) => {
  const body = req.body;
  processRequest(body, 'dataset', req, res);
});

app.post('/element', (req, res) => {
  // Extract the data from the request
  const { requestId, domain, element } = req.body;

  processRequest(element, 'element', req, res);

  // Send a response
  res.json({
    requestId,
    domain,
    element,
  });
});

app.post('/dataset', (req, res) => {
  // Extract the data from the request
  const { requestId, domain, dataset } = req.body;

  processRequest(dataset, 'dataset', req, res);

  // Send a response
  res.json({
    requestId,
    domain,
    dataset,
  });
});

function processRequest(body, artifactType, req, res) {
  // Validate the element against the Data.schema.json schema
  // You can use a JSON schema validator library like Ajv for this

  saveToFile(body, artifactType, (err) => {
    if (err) {
      console.error(err);

      const event = {
        "RequestFailed": body
      };

      res.status(500).send(JSON.stringify(event));
    } else {

      const event = {
        "Created": body
      };

      res.status(201).send(JSON.stringify(event));
    }
  });
}

function saveToFile(artifact, artifactType, callback) {
  return new Promise(() => {
    try {
      storage.write(artifact.id, artifactType, artifact);
      callback(null);
    } catch (err) {
      callback(err);
    }
  });
}


app.listen(3000, () => {
  console.log('Server is running on port 3000');
});
