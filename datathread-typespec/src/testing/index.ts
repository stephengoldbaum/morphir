import { resolvePath } from "@typespec/compiler";
import { createTestLibrary, TypeSpecTestLibrary } from "@typespec/compiler/testing";
import { fileURLToPath } from "url";

export const DatathreadTypespecTestLibrary: TypeSpecTestLibrary = createTestLibrary({
  name: "datathread-typespec",
  packageRoot: resolvePath(fileURLToPath(import.meta.url), "../../../../"),
});
