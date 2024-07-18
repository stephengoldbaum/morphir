import { createTypeSpecLibrary } from "@typespec/compiler";

export const $lib = createTypeSpecLibrary({
  name: "datathread-typespec",
  diagnostics: {},
});

export const { reportDiagnostic, createDiagnostic } = $lib;
