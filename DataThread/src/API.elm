module DataThread.API exposing (..)

import Data exposing (..)

type Scheme = element | dataset

type alias Domain = List String

type alias URI = 
    { scheme : Scheme
    , domain : Domain
    , name : String
    }

uriToID : URI -> ID
uriToID uri = 
    uri.scheme ++ ":" ++ (String.join "/" uri.domain) ++ ":" ++ uri.name

type alias RequestFailed =
    { requestId: ID
    , error: String
    }


---- Elements
type alias CreateElement =
    { requestId: ID
    , domain : Domain
    , element : Element
    }

type alias ElementCreated =
    { requestId: ID
    , domain: Domain
    , element : Element
    }

createElement : CreateElement -> Cmd Msg
-- updateElement : Domain -> Element -> Cmd Msg
-- deleteElement : ElementID -> Cmd Msg
-- moveElement : ElementID -> Domain -> Cmd Msg
-- renameElement : ElementID -> ElementID -> Cmd Msg 

setElementConstraints : ElementConstraints -> Cmd Msg

---- Datasets
type alias CreateDataset =
    { requestId: ID
    , domain : Domain
    , dataset : Dataset
    }

type alias DatasetCreated =
    { requestId: ID
    , domain: Domain
    , dataset : Dataset
    }

createDataset : CreateDataset -> Cmd Msg
-- updateDataset : Dataset -> Cmd Msg
-- deleteDataset : DatasetID -> Cmd Msg

---- Finance
-- linkToElement : ElementID -> A -> Cmd Msg
-- unlinkFromElement : ElementID -> A -> Cmd Msg

-- linkToDataset : DatasetID -> A -> Cmd Msg
-- unlinkFromDataset : DatasetID -> A -> Cmd Msg

---- Clear
type alias ClearAll =
    { requestId: ID
    , domain : Maybe Domain
    }

type alias ClearedAll =
    { requestId: ID
    , domain : Maybe Domain
    }

clearAll : ClearAll -> Cmd Msg
