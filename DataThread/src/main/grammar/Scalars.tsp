import "@typespec/json-schema";
using TypeSpec.JsonSchema;

@jsonSchema
namespace DataThread.Grammar;

scalar Name extends string;

scalar Version extends string;

scalar Description extends string;

scalar DisplayName extends string;
scalar ShortDisplayName extends string;

