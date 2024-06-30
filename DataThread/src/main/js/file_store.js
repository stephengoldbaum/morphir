const fs = require('fs');
const path = require('path');
const { log } = require("console");

class Storage {
    constructor(baseDir) {
        this.baseDir = baseDir;
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

/**
 * Converts a URN (Uniform Resource Name) to a file path.
 * @param {string} urn - The URN to convert.
 * @param {string} [typ] - The file type extension. If not provided, it will be extracted from the URN.
 * @returns {string} The file path corresponding to the URN.
 */
function urnToFile(baseDir, urn, typ) {
  const items = urn.split(':');

  if(typ === undefined || typ == null) {
    typ = items[0];
  }

  const file = path.join(baseDir, `/${items[1]}`, `${items[2]}.${typ}.json`)
  return file;
}

/**
 * Retrieves JSON data from a file.
 *
 * @param {string} id - The URN of the file.
 * @param {string} typ - The file type extension.
 * @returns {object} The JSON data from the file.
 */
 function resolveAndReadFromDir(baseDir, id, typ) {
  const file = urnToFile(baseDir, id, typ);
  return inflate(file);
}

/**
 * Reads a file and parses its content as JSON.
 *
 * @param {string} file - The path to the file.
 * @returns {object} The parsed JSON data, or undefined if the file does not exist.
 */
 function inflate(file) {
  if (fs.existsSync(file)) {
    const data = fs.readFileSync(file);
    return JSON.parse(data);
  } else {
    return undefined;
  }
}


//////// Module ///////////
module.exports = {
    Storage
};
