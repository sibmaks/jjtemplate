package io.github.sibmaks.jjtemplate.compiler.benchmark;

/**
 * Representative template shapes used by compilation and rendering benchmarks.
 */
public enum BenchmarkScenario {
    STATIC_LITERAL,
    SCALAR_SUBSTITUTION,
    NESTED_MAP,
    FUNCTION_PIPELINE,
    CONDITIONALS,
    COLLECTION,
    REALISTIC_DOCUMENT
}
