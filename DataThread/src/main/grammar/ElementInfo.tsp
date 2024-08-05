import "./Element.tsp";
import "@typespec/json-schema";
using TypeSpec.JsonSchema;
using DataThread.Grammar.Element;

@jsonSchema
namespace DataThread.Grammar.ElementInfo;

model ElementInfo  {
    id: ElementID;
    description?: string;
    display_name?: string;
    short_display_name?: string;
};
