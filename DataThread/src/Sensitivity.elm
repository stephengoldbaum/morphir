module Sensitivity exposing (..)

type SensitivityLevel
    = PII
    | PI
    | SPI
    | NPI
    | MNPI

type alias Mask = String

type alias Sensitivity =
    { level : SensitivityLevel
    , mask : Mask
    }