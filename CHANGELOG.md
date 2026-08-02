# Changelog

All notable changes to JJTemplate are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Added map iteration to range expressions, exposing each entry as `key,value`; collection and array ranges continue
  to expose `item,index`.

## [1.0.0-rc2] - 2026-08-02

### Added

- Added Gradle dependency locking for the root build, `buildSrc`, and all published modules.
- Added source and binary compatibility checks for the supported public API, using `1.0.0-rc1` as the baseline.
- Added an explicit public API policy and generated Javadocs for supported types.
- Added runnable Maven and Gradle quick starts and documented custom function registration and invocation.

### Changed

- Release publication now depends on the complete verification gate, including tests, Checkstyle, Javadocs, and API
  compatibility checks.
- Refactored lexer and reflection method matching code to reduce complexity without changing template behavior.
- Improved test readability and maintainability across compiler, parser, reflection, and function coverage.

### Fixed

- Fixed API baseline resolution during versioned release builds so compatibility checks use the published
  `1.0.0-rc1` artifacts instead of current subprojects.
- Fixed prerelease dependency lock placement after isolating published API baselines from project substitution.
- Corrected the `.DS_Store` ignore rule.

## [1.0.0-rc1] - 2026-08-01

### Added

- Added a comprehensive JMH suite for cold compilation, rendering, concurrent rendering, scaling, type binding, and
  compiler lifecycle scenarios.
- Added caching for resolved property accessors.
- Added GitHub prerelease classification for release-candidate and beta versions.

### Changed

- Reflection metadata caches now use `ClassValue` so cache entries follow the lifetime of their class loader.
- Definition keys are parsed consistently with regular template keys.

### Fixed

- Fixed property access for `Map.Entry` implementations returned by concurrent maps on modular JDKs.

### Removed

- Removed `definitionExpressionFallback` and the legacy definition parser. `switch` and `range` definition keys must
  now use explicit `{{ ... }}` expression wrappers.

## [0.9.2] - 2026-07-27

### Added

- Added lazy function argument evaluation through `TemplateFunction.isLazy()`.
- Added lazy behavior to `default`, `and`, and `or`, including boolean short-circuit evaluation.
- Added a benchmark for compile-time type context performance.

### Fixed

- Fixed constant folding of lazy functions so unused arguments are not evaluated.

## [0.9.1] - 2026-06-25

### Added

- Added `GregorianCalendar` support to `date:format`.

### Fixed

- Fixed a template expression folding case that could repeatedly fold the same expression without making progress.
- Added regression coverage for the infinite-folding scenario.

## [0.9.0] - 2026-06-22

### Added

- Added compile-time variable and property type validation with `STRICT` and `SOFT` modes.
- Added `MapTemplateCompileContext` and support for custom field resolution in strict validation mode.
- Added configurable default locale support for locale-sensitive date and string functions.
- Added a generated compile-time registry for built-in functions, replacing runtime classpath scanning.

### Changed

- Split reflection, parser, and type-binding responsibilities into focused components.
- Expanded compiler, parser, lexer, reflection, folding, and type-binding test coverage.
- Updated Gradle and project dependencies.

### Fixed

- Fixed list and object expression folding when only part of an expression can be reduced at compile time.
- Fixed Javadoc generation issues in runtime and folding APIs.

## [0.8.1] - 2026-03-24

### Fixed

- Fixed `eq` and `neq` comparisons between numerically equal values represented by different Java `Number` types.

## [0.8.0] - 2026-03-12

### Added

- Added source rendering for compiled expressions so runtime failures include the expression that caused the error.
- Added integration coverage for date and datetime formatting expressions.

[Unreleased]: https://github.com/sibmaks/jjtemplate/compare/v1.0.0-rc1...HEAD
[1.0.0-rc2]: https://github.com/sibmaks/jjtemplate/compare/v1.0.0-rc1...v1.0.0-rc2
[1.0.0-rc1]: https://github.com/sibmaks/jjtemplate/compare/v0.9.2...v1.0.0-rc1
[0.9.2]: https://github.com/sibmaks/jjtemplate/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/sibmaks/jjtemplate/compare/v0.9.0...v0.9.1
[0.9.0]: https://github.com/sibmaks/jjtemplate/compare/v0.8.1...v0.9.0
[0.8.1]: https://github.com/sibmaks/jjtemplate/compare/v0.8.0...v0.8.1
[0.8.0]: https://github.com/sibmaks/jjtemplate/compare/v0.7.1...v0.8.0
