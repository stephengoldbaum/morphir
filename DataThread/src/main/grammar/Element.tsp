namespace DataThread.Grammar.Element;


model Element  {
    id: ElementID;
    name: string;
    element_type?: ElementType;
};

alias ElementType = Text | Number | Date | Time | DateTime | Boolean | Enum | Record | Reference;

scalar ElementID extends url;

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
