import "./Element.tsp";

namespace DataThread.Grammar.Dataset;

using DataThread.Grammar.Element;

scalar DatasetID extends url;

model Dataset {
  id: DatasetID;
  name: string;
  version: string;
  fields: Array<Field>;
}