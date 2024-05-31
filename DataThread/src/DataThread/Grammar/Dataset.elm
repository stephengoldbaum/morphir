module DataThread.Grammar.Dataset exposing (..) 

import DataThread.Grammar.ID exposing (ID)
import DataThread.Grammar.Element exposing (Fields)


type alias DatasetID = ID

type alias FieldName = String

type alias Dataset = 
    { id : DatasetID
    , name : String
    , version: String
    , fields : Fields
    }

type alias Key = 
    { dataset : DatasetID
    , fields : Fields
    }
