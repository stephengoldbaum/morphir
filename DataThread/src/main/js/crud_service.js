const fs = require('fs');
const path = require('path');
const { log } = require("console");

class CRUDService {
    constructor(storage) {
        this.storage = storage;
    }

    findAll(pattern) {
        return recursiveSearch(this.baseDir, pattern);
    }

    resolveAndRead(id, typ) {
        return resolveAndReadFromDir(this.baseDir, id, typ);
    }

    findAllAndRead(pattern) {
        return this
            .findAll(pattern)
            .map(file => inflate(file));
    }

    write(id, typ, data) {
      const file = urnToFile(this.baseDir, id, typ);
      fs.writeFileSync(file, JSON.stringify(data));
    }
  }
