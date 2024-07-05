import "@typespec/json-schema";

using TypeSpec.JsonSchema;

@jsonSchema
namespace DataThread.Grammar;

scalar URI extends string;
scalar ID extends URI;

scalar ElementID extends ID;

model TextConstraints {
    min_length?: int64;
    max_length?: int64;
  
};

model NumberConstraints {
    minimum?: int64;
    maximum?: int64;
    precision?: int64;
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
  value:ElementID;
  optional: boolean;
}

model Record { 
  fields: Field;
}

scalar Date extends string;
scalar Time extends string;
scalar DateTime extends string;
scalar Boolean extends string;

alias ElementType = Text | Number | Date | Time | DateTime | Boolean | Enum | Record | Reference;

model Element  {
    id: ElementID;
    name: string;
    element_type?: ElementType;
};

model ElementInfo  {
    id: ElementID;
    description?: string;
    display_name?: string;
    short_display_name?: string;
};

scalar DatasetID extends ID;

model Dataset {
  id: DatasetID;
  name: string;
  version: string;
  fields: [Field];
}
