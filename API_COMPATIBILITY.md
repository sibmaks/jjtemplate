# API compatibility policy

JJTemplate follows semantic versioning starting with `1.0.0`. Public types in the
packages listed below are supported API. Their binary and source compatibility is
checked against the version configured by `api_baseline_version` in
`gradle.properties`.

## Supported API

### `jjtemplate-lexer`

- `io.github.sibmaks.jjtemplate.lexer`
- `io.github.sibmaks.jjtemplate.lexer.api`

### `jjtemplate-parser`

- `io.github.sibmaks.jjtemplate.parser`
- `io.github.sibmaks.jjtemplate.parser.api`
- `io.github.sibmaks.jjtemplate.parser.exception`

### `jjtemplate-compiler`

- `io.github.sibmaks.jjtemplate.compiler.api`
- `io.github.sibmaks.jjtemplate.compiler.exception`
- `io.github.sibmaks.jjtemplate.compiler.runtime`
- `io.github.sibmaks.jjtemplate.compiler.runtime.exception`
- `io.github.sibmaks.jjtemplate.compiler.runtime.fun`

Subpackages are not included unless they are listed explicitly. In particular,
types under `impl`, `optimizer`, `expression`, `reflection`, and `visitor` are
implementation details even when Java visibility makes them accessible.

## Compatibility check

Run the complete verification gate:

```shell
./gradlew check
```

Or run the API check alone:

```shell
./gradlew apiCompatibilityCheck
```

The baseline must be the latest published release that promises compatibility.
Update `api_baseline_version` only when preparing the next release line. An
intentional incompatible change requires a new major version and corresponding
migration notes.
