# JJTemplate

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=bugs)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sibmaks_jjtemplate&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sibmaks_jjtemplate)

**JJTemplate** is a lightweight templating engine focused on **minimal render time** and
**JSON-compatible input/output**.  
It compiles templates into optimized abstract syntax trees (ASTs) for fast execution with predictable, valid JSON
results.

## Project Modules

- **Lexer** — converts raw template strings into structured tokens.
- **Parser** — builds abstract syntax trees (AST) from token sequences.
- **Compiler** — transforms parsed templates into executable node trees.
    - **Optimizer** — applies static optimizations to improve runtime performance.
- **Evaluator** — executes expressions and renders final output.

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

- `.varName` — access variable values

- `{{ expr }}` — direct expression substitution

- `{{? expr }}` — conditional insertion (skips if null)

- `{{. expr }}` — spread values into arrays or objects

Supports expressions, pipe calls, and ternary operators (`?`, `:`).

## Supported Features

Variable definitions: static, conditional (`case`), and range-based (`range`)

Built-in functions: `str`, `int`, `float`, `boolean`, `len`, `empty`, `not`, `eq`, `neq`, `lt`, `le`, `gt`, `ge`, `and`,
`or`, `list`, `concat`, `default`, `collapse`, `format`, `formatDate`, `parseDate`, `parseDateTime`

Composable expressions: via `|` (pipe operator)

Ternary conditions: `condition ? value1 : value2`

See more examples [here](examples.md).

# Goals

* Minimize template render time
* Maintain clean separation between parsing, compilation, and execution
* Provide predictable, optimized output generation
* Guarantee valid JSON for both template input and generated output.