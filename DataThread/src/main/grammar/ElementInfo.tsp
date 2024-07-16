import "./Element.tsp";

namespace DataThread.Grammar.ElementInfo;

using DataThread.Grammar.Element;

scalar DatasetID extends url;

model ElementInfo  {
    id: ElementID;
    description?: string;
    display_name?: string;
    short_display_name?: string;
};
