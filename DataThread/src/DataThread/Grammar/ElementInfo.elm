module DataThread.Grammar.ElementInfo exposing (..)

import DataThread.Grammar.Element exposing (ElementID)


type alias ElementInfo =
    { id : ElementID
    , description : Maybe String
    , display_name : Maybe String
    , short_display_name : Maybe String
    }
