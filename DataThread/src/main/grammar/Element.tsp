import "./ElementType.tsp";
import "./Scalars.tsp";

import "@typespec/json-schema";
using TypeSpec.JsonSchema;

@jsonSchema
namespace DataThread.Grammar.Element;

//scalar ElementID extends url;
scalar ElementID extends string;

model Element  {
    id: ElementID;
    name: Name;
    element_type: ElementType;
};
