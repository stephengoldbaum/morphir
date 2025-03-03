# FAIR (Functional Algebraic IR) Specification

## Overview

FAIR is a specification format designed for describing functional programming language intermediate representations (IRs). It provides a declarative way to define type systems, value representations, and semantic rules with a focus on algebraic data types and pattern matching.

## Key Features

- Native support for functional programming concepts
- Generic attributes for compiler metadata
- Explicit semantic rules and constraints
- Type-safe definitions
- Pattern matching and exhaustiveness checking

## Basic Syntax

### Definitions

```fair
def Name {
  // Properties are defined with their types
  property: Type
  
  // Variants use the 'variant' keyword
  variant {
    VariantA { /* ... */ }
    VariantB { /* ... */ }
  }
}
```

### Generic Parameters

```fair
def Container<T> {
  value: T
}
```

### Rules

```fair
rules {
  RuleName {
    // Rule conditions and constraints
    forall x: Type {
      condition
    }
  }
}
```

## Core Concepts

### 1. Names and Paths

Names and paths are fundamental building blocks:

```fair
def Name {
  segments: List<String>
}

def Path {
  segments: List<Name>
}

def FQName {
  packagePath: Path
  modulePath: Path
  localName: Name
}
```

### 2. Types

Types can be simple or complex:

```fair
def Type<Attributes> {
  variant {
    Variable { /* ... */ }
    Reference { /* ... */ }
    // ...
  }
}
```

### 3. Values

Values represent both data and computation:

```fair
def Value<TypeAttrs, ValueAttrs> {
  variant {
    Literal { /* ... */ }
    Constructor { /* ... */ }
    // ...
  }
}
```

### 4. Patterns

Patterns support pattern matching:

```fair
def Pattern<Attributes> {
  variant {
    WildcardPattern { /* ... */ }
    AsPattern { /* ... */ }
    // ...
  }
}
```

## Semantic Rules

FAIR includes explicit semantic rules:

1. **Type Checking**
   - Every value must have a corresponding type
   - Pattern matching must be exhaustive

2. **Name Resolution**
   - All references must point to defined entities
   - No circular dependencies

## Best Practices

1. **Type Safety**
   - Always specify generic parameters
   - Use explicit attributes for metadata

2. **Pattern Matching**
   - Ensure patterns are exhaustive
   - Order patterns from specific to general

3. **Names and References**
   - Use fully qualified names for external references
   - Keep path segments meaningful and hierarchical

## Tool Support

FAIR specifications can be used to generate:

1. **Validators**
   - Type checkers
   - Pattern match exhaustiveness checkers
   - Name resolution validators

2. **Code Generators**
   - Language-specific implementations
   - Serialization formats
   - Documentation

3. **Analysis Tools**
   - Dead code detection
   - Type inference
   - Optimization opportunities

## Example Usage

Here's a complete example of a simple type definition:

```fair
def Option<T> {
  variant {
    Some {
      value: T
    }
    None
  }
}

rules {
  OptionPatternMatch {
    forall match: PatternMatch {
      where subject: Option<T> {
        must cover Some, None
      }
    }
  }
}
```

## Version History

- 1.0.0: Initial specification
  - Core type system
  - Value representation
  - Pattern matching
  - Semantic rules
