#!/usr/bin/env node

// NPM imports
import { Command } from "commander";
import cli from "./cli";

// logging
require("log-timestamp");

// Set up Commander
export const command = new Command();

command
  .name("stats")
  .description("Collect morphir features used in a model into a document")
  .option(
    "-i, --input <path>",
    "Source location where the Morphir IR will be loaded from.",
    "morphir-ir.json"
  )
  .option(
    "-o, --output <path>",
    "Target location where the generated code will be saved.",
    "./stats"
  )
  .action((options) => {
    const { input: inputPath, output: outputPath } = options;

    cli
      .stats(inputPath, outputPath, command.opts())
      .then(() => {
        console.log("Done");
      })
      .catch((err) => {
        console.log(err);
        process.exit(1);
      });
  });
