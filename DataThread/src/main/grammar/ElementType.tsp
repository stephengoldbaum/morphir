import "@typespec/json-schema";
using TypeSpec.JsonSchema;

@jsonSchema
namespace DataThread.Grammar.Element;


union ElementType { Text , Number , Date , Time , DateTime , Boolean , Enum , Record , Reference }
//alias ElementType = Text | Number | Date | Time | DateTime | Boolean | Enum | Record | Reference;

model TextConstraints {
    min_length?: integer;
    max_length?: integer;
  
};

model NumberConstraints {
    minimum?: integer;
    maximum?: integer;
    precision?: integer;
};

model Text { 
  constraints : TextConstraints
}

model Number {
  constraints : NumberConstraints
}

model Enum { 
  values : string[]
}

model Reference {
  element: ElementID
}

model Field {
  name:string;
  element:ElementID;
  optional: boolean;
}

model Record { 
  fields: Field;
}

scalar Date extends string;
scalar Time extends string;
scalar DateTime extends string;
scalar Boolean extends string;

