import "./Element.tsp";
import "./Scalars.tsp";

import "@typespec/json-schema";
using TypeSpec.JsonSchema;
using DataThread.Grammar.Element;

@jsonSchema
namespace DataThread.Grammar.ElementInfo;

model ElementInfo  {
    id: ElementID;
    description?: Description;
    display_name?: DisplayName;
    short_display_name?: ShortDisplayName;
};
