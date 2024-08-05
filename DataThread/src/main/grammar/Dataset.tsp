import "./ElementType.tsp";
import "@typespec/json-schema";
using TypeSpec.JsonSchema;
using DataThread.Grammar.Element;

@jsonSchema
namespace DataThread.Grammar.Dataset;

//scalar DatasetID extends url;
scalar DatasetID extends string;

model Dataset {
  id: DatasetID;
  name: string;
  version: string;
  fields: Array<Field>;
}