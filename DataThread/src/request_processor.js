const express = require('express');
const fs = require('fs');
const path = require('path');
const app = express();
const bodyParser = require('body-parser');
const { log } = require('console');

const baseDirArg = process.argv.includes('--baseDir') 
  ? process.argv[process.argv.indexOf('--baseDir') + 1] 
  : 'metastore';

const baseDir = path.resolve(process.cwd(), baseDirArg);
log("Using base folder: " + baseDir);

if(!fs.existsSync(baseDir)) {
  fs.mkdirSync(baseDir);
}

console.log("Using base folder: " + baseDir);

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

  saveToFile(body, (err) => {
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

function saveToFile(artifact, callback) {
  // Save as a JSON file in the data folder
  const items = artifact.id.split(':');
  const typ = items[0];
  const domain = items[1].startsWith('/') ? items[1].slice(1) : items[1];
  const name = items[2];

  // Split the domain into an array of folder names
  const folders = domain.split('/');

  // Create each folder if it doesn't already exist
  folders.reduce((folderPath, folder) => {
    const currentPath = path.join(folderPath, folder);
    if (!fs.existsSync(currentPath)) {
      fs.mkdirSync(currentPath);
    }
    return currentPath;
  }, baseDir);

  const folder = path.join(baseDir, ...folders);
  const fileName = path.join(folder, `${name}.${typ}.json`);
  log(fileName)
  const json = JSON.stringify(artifact);

  fs.writeFile(fileName, json, callback);
}

app.listen(3000, () => {
  console.log('Server is running on port 3000');
});
