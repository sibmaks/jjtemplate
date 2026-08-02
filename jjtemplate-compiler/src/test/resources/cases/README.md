# Integration test cases

Each leaf directory containing an `input.jjt` file is an integration test case.
The test harness discovers cases recursively, so grouping directories may also
contain documentation.

A case contains:

- `input.jjt` — template script to compile and render;
- `expected.json` — expected rendered value;
- `variables.json` — optional render context.

Cases are grouped by their primary purpose:

- `language` — template-language semantics;
- `functions` — built-in function behavior, grouped by namespace;
- `optimization` — compiler optimization and static-compilation behavior;
- `regressions` — scenarios retained for previously fixed defects.

Use kebab-case names and organize new cases as
`<area>/<feature>/<observable-scenario>`. Prefer names that describe externally
visible behavior, such as `lists/conditional-element/null-value`, over generic
names such as `simple` or `case-1`.
