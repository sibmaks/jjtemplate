# JJTemplate

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=bugs)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)

> ⚠️ Project is under active development. Backwards compatibility is not guaranteed.

**JJTemplate** is a lightweight templating engine designed for **minimal render time** and **JSON-compatible
input/output**. It compiles templates into optimized abstract syntax trees (ASTs) for fast execution while guaranteeing
valid JSON results.

## Usage

### Maven

```xml

<dependency>
    <groupId>io.github.sibmaks.jjtemplate</groupId>
    <artifactId>jjtemplate</artifactId>
    <version>0.3.0</version>
    <type>pom</type>
</dependency>
```

### Gradle

```kotlin
implementation("io.github.sibmaks.jjtemplate:jjtemplate:0.3.0")
```

## Example Workflow

1. **Compile a template**
   ```java
   var compiler = TemplateCompiler.getInstance();
   var script = new TemplateScript(...);
   var compiled = compiler.compile(script);
   ```
2. Render with data
    ```java
    var result = compiled.render(Map.of("name", "Alice"));
    ```

## Template Format

Templates are written in pure JSON with embedded expressions using double curly braces:

```json
{
  "definitions": [
    {
      "greeting": "{{ concat 'Hello, ', .name }}"
    }
  ],
  "template": {
    "message": "{{ .greeting }}"
  }
}
```

### Expression Types

- `.varName` — access variable values

- `{{ expression }}` — direct expression substitution

- `{{? expression }}` — conditional insertion (skips if null)

- `{{. expression }}` — spread values into arrays or objects

Supports expressions, pipe calls, and ternary operators (`?`, `:`).

## Core Concepts

### Variables and Access

- `.varName` - Access variable values from context
- Supports nested object access (e.g., `.user.profile.name`)

Variable definitions: static, conditional (`switch`), and range-based (`range`)

## Built-in Functions

### Type Converters

- `str(value)` - Convert any value to string
- `int(value)` - Convert to integer
- `float(value)` - Convert to float
- `boolean(value)` - Convert to boolean

### Logical Operations

- `not(value)` - Boolean inversion
- `eq(a, b)`, `neq(a, b)` - Equality checks
- `lt(a, b)`, `le(a, b)`, `gt(a, b)`, `ge(a, b)` - Comparisons
- `and(a, b)`, `or(a, b)`, `xor(a, b)` - Logical operations

### Collection Operations

- `list(...items)` - Create lists
- `len(collection)` - Get length/size
- `empty(value)` - Check if empty
- `concat(...values)` - Concatenate values
- `contains(container, ...values)` - Membership checking
- `collapse(array)` - Merge array of objects

### Utilities

- `default(value, fallback)` - Provide default values
- `format(pattern, ...args)` - String formatting
- `formatDate(date, pattern)` - Date formatting
- `parseDate(string, pattern)` - Date parsing

### String Operations

- `concat(object, ...values)` - Concat objects to string
- `len(string)` - Get string length
- `empty(string)` - Check if empty
- `contains(string, ...values)` - String contains checking

Composable expressions: via `|` (pipe operator)

Ternary conditions: `condition ? value1 : value2`

See more examples [here](examples.md).

## Architecture

JJTemplate is built with a modular architecture:

- **Lexer** - Tokenizes template strings
- **Parser** - Constructs AST from tokens
- **Compiler** - Generates executable node trees
    - **Optimizer** - Applies performance optimizations
- **Evaluator** - Executes templates and produces output

## Goals

- **Minimal render time** through AST optimization
- **Clean separation** of parsing, compilation, and execution
- **Predictable output** with JSON compatibility guarantees
- **Optimized performance** at every processing stage