# JJTemplate benchmarks

The benchmark suite separates compilation, rendering and practical lifecycle
costs. Results use latency units for single-threaded work and throughput only
for the explicitly concurrent benchmark.

## Questions answered

| Benchmark | Question |
|---|---|
| `CompileBenchmark` | How expensive is repeated compilation for each template shape? |
| `ColdCompileBenchmark` | How expensive is compiler creation and the first compilation in a fresh JVM? |
| `RenderBenchmark` | How expensive is one render after compilation? |
| `LifecycleBenchmark` | Does optimization pay off after 1, 10 or 1000 renders? |
| `TypeBindingBenchmark` | What do bound DTO properties and methods cost compared with dynamic lookup? |
| `TypeBindingLifecycleBenchmark` | After how many renders does explicit type binding repay its compilation cost? |
| `ScalingBenchmark` | How do compile and render costs change with range input cardinality and inline versus external data? |
| `ConcurrentRenderBenchmark` | How does one compiled template behave under shared concurrent rendering? |

`ScalingBenchmark` labels data placement explicitly. `EXTERNAL` keeps compile
work constant and grows render work; `INLINE` moves collection materialization
and folding into compilation, making the compile/render trade-off visible.

`Map` properties intentionally remain dynamic. The type-binding suite therefore
uses real DTO property, method and polymorphic receiver fixtures, plus a separate
`MAP_FALLBACK` control case.

All scenarios are shared with `BenchmarkScenarioSemanticsTest`. The normal test
suite checks expected output, optimized/unoptimized equivalence, typed/untyped
equivalence, scaling cardinality and the actual bound or dynamic expression path.

## Run profiles

Benchmark classes and production code continue to compile with Java 11 bytecode.
The `jmhQuick` and `jmhFull` execution tasks use a JDK 21 Gradle toolchain so the
measured JVM cannot silently fall back to the JVM that launched Gradle.

Run the quick profile with:

```shell
./gradlew --no-daemon :jjtemplate-compiler:jmhQuick
```

`jmhQuick` runs compile and render for a small and realistic scenario with one
fork and short iterations. It is intended as a smoke test, not a publishable
measurement.

```shell
./gradlew --no-daemon :jjtemplate-compiler:jmhFull
```

`jmhFull` respects the benchmark annotations and enables the JMH GC profiler.
It writes JSON and human-readable output under `build/reports/jmh`.

## Reports and comparison

Create Markdown from the latest full result:

```shell
./gradlew :jjtemplate-compiler:jmhReport
```

Choose another result or output path with `-PjmhResults=...` and
`-PjmhReport=...`.

Compare a result with a saved baseline:

```shell
./gradlew :jjtemplate-compiler:jmhCompare \
  -PjmhBaseline=/path/to/baseline.json \
  -PjmhResults=/path/to/current.json
```

Type-binding results use `binding=DYNAMIC` when no root type is supplied and
`binding=EXPLICIT_CONTEXT` when a `TemplateCompileContext` supplies it. Once the
root type is known, the compiler resolves the remaining property or method chain.

The report records the commit and host, displays normalized GC allocation when
available, compares matching results with the baseline, and calculates in-run
deltas for optimizer and typed-context variants. Confidence intervals remain in
the table; small deltas inside the reported error should not be treated as a
regression or improvement.

Full JMH runs are deliberately not a pull-request pass/fail gate because shared
CI runners are noisy. Compilation, Checkstyle and semantic validation remain
part of the regular Gradle build.
