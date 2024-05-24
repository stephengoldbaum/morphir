module DataThread.Grammar.Element exposing (..)

import DataThread.ID exposing (ID)


type alias ElementID = ID

type ElementType
    = Text (Maybe TextConstraints)
    | Number (Maybe NumberConstraints)
    | Date
    | Time
    | DateTime
    | Boolean
    | Enum (List String)
    | Reference ElementID

type alias TextConstraints =
    { min_length : Maybe Int
    , max_length : Maybe Int
    }

type alias NumberConstraints =
    { minimum : Maybe Int
    , maximum : Maybe Int
    , precision : Maybe Int
    }

type alias Integer = Number (
    Just {
        minimum = Just -2147483648
        , maximum = Just 2147483647
        , precision = Just 0
    }
)

type alias Element = 
    { id : ElementID
    , name : String
    , element_type : ElementType
    }

type alias ElementInfo =
    { id : ElementID
    , description : Maybe String
    , display_name : Maybe String
    , short_display_name : Maybe String
    }

type alias Elements = List Element