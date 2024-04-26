module DataThread.Dataset exposing (..) 

import DataThread.ID exposing (ID)
import DataThread.Element exposing (ElementID)


type alias DatasetID = ID

type alias FieldName = String

type alias ElementOrbit = ElementID

type alias Field = 
    { name : FieldName
    , element : ElementOrbit
    , optional : Maybe Bool
    , key : Maybe Bool
    }

type alias Dataset = 
    { id : DatasetID
    , name : String
    , version: Int
    , fields : List Field
    }
