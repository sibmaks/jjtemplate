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
      "greeting": "{{ string:concat 'Hello, ', .name }}"
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

Functions are organized into **namespaces** by type or purpose.
Call syntax uses a colon (`:`), e.g. `{{ cast:str .value }}` or `{{ .text | string:upper }}`.

---

### `cast` — Type Conversions

* `cast:str(value)` — Convert to string
* `cast:int(value)` — Convert to integer (`BigInteger`)
* `cast:float(value)` — Convert to decimal (`BigDecimal`)
* `cast:boolean(value)` — Convert to boolean

---

### `string` — String Operations

* `string:concat(base, ...values)` — Concatenate strings
* `string:join(glue, ...values)` — Concatenate strings with glue between values
* `string:joinNotEmpty(glue, ...values)` — Concatenate strings with glue between values, skip null and empty values
* `string:len(string)` — Get string length
* `string:empty(string)` — Check if empty or null
* `string:contains(string, ...substrings)` — Check if all substrings exist in string
* `string:format([locale], pattern, ...args)` — Format string (like `String.format`)
* `string:lower([locale], value)` — Convert to lowercase
* `string:upper([locale], value)` — Convert to uppercase
* `string:trim(value)` — Remove all leading and trailing space
* `string:split(value, regex, [limit])` — Splits this string around matches of the given regular expression.
* `string:indexOf(value, str)` — Returns the index within this string of the first occurrence of the specified
  substring.
* `string:lastIndexOf(value, str)` — Returns the index within this string of the last occurrence of the specified
  substring.
* `string:substr(value, beginIndex, [endIndex])` — Returns a string that is a substring of this string. Support negative indexes.
* `string:replace(value, target, replacement)` — Replaces each substring of this string that matches the literal target
  sequence with the specified literal replacement sequence.
* `string:replaceAll(value, regex, replacement)` — Replaces each substring of this string that matches the given regular
  expression with the given replacement.
  The substring begins at the specified `beginIndex` and extends to the character at index `endIndex - 1`.

---

### `list` — List / Array Operations

* `list:new(...items)` — Create a list
* `list:concat(...lists)` — Concatenate multiple lists or arrays
* `list:len(list)` — Get size
* `list:empty(list)` — Check if empty
* `list:contains(list, ...values)` — Check if list contains all values
* `list:head(list)` — Get head of list or null
* `list:tail(list)` — Get tail of list or empty list
* `list:join(glue, ...lists)` — Join all lists into single string

---

### `map` — Map / Object Operations

* `map:new(key, value, ...)` — Create a map
* `map:len(map)` — Get number of entries
* `map:empty(map)` — Check if empty
* `map:contains(map, ...keys)` — Check if all keys exist
* `map:collapse(object|array|collection)` — Merge object properties into one map

---

### `date` — Date Utilities

* `date:format([locale], pattern, date)` — Format date (`Date`, `LocalDate`, `LocalDateTime`, `ZonedLocalDateTime`)
* `date:parse(pattern, string)` — Parse string into `LocalDate`
* `date:now()` — Get current `LocalDate`

---

### `datetime` — DateTime Utilities

* `datetime:parse(pattern, string)` — Parse string into `LocalDateTime`
* `datetime:now()` — Get current `LocalDateTime`

---

### `locale` — Locale Utilities

* `locale:new(language[, country[, variant]])` — Create a `Locale` instance

---

### `math` — Math Operations

* `math:neg(value)` — Negate numeric value
* `math:sum(left, right)` — Sum two numeric values
* `math:sub(left, right)` — Subtract two numeric values
* `math:mul(left, right)` — Multiply two numeric values
* `math:div(left, right, [mode])` — Divide two numeric values and scale using passed `mode`.
* `math:scale(value, amount, mode)` — Returns a `float` whose scale is the specified value, and whose unscaled value is
  determined by multiplying or dividing this `float`'s unscaled value by the appropriate power of ten to maintain its
  overall value.

---

### `default`

* `default(value, fallback)` — Return fallback if value is `null`

---

### Logical and Comparison Operators

(These remain **global**, without namespace.)

* `not(value)` — Boolean inversion
* `eq(a, b)`, `neq(a, b)` — Equality checks
* `lt(a, b)`, `le(a, b)`, `gt(a, b)`, `ge(a, b)` — Comparisons
* `and(a, b)`, `or(a, b)`, `xor(a, b)` — Logical operations

---

### Notes

* All functions can be used **in pipe form**, e.g.

  ```json
  { "upperName": "{{ .name | string:upper }}" }
  ```
* Namespace separation ensures no name collisions and improves clarity.

---

### `?` Conditional Expressions (Ternary Operator)

JJTemplate supports inline conditional expressions using the ternary operator:

```text
condition ? valueIfTrue : valueIfFalse
```

The operator evaluates the condition and returns one of two values:

* If the condition is **true**, the expression before the colon (`:`) is returned.
* If the condition is **false** or `null`, the expression after the colon is returned.

#### Example

```json
{
  "status": "{{ eq .ge 18 ? 'adult' : 'minor' }}"
}
```

If `.age >= 18`, the result will be:

```json
{
  "status": "adult"
}
```

Otherwise:

```json
{
  "status": "minor"
}
```

---

#### Expressions inside ternary

Both `condition` and results (`valueIfTrue` / `valueIfFalse`)
can contain **any expression**, including function calls and pipes:

```json
{
  "greeting": "{{ .isMorning ? string:upper 'good morning' : string:upper 'good evening' }}"
}
```

or with pipe syntax:

```json
{
  "formatted": "{{ .amount | gt 1000 ? 'large' : 'small' }}"
}
```

---

#### Nesting

Ternary expressions can be **nested** for compact logic:

```json
{
  "label": "{{ eq .type 'a' ? 'Alpha' : eq .type 'b' ? 'Beta' : 'Other' }}"
}
```

---

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
