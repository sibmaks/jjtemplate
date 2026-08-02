package io.github.sibmaks.jjtemplate.compiler.api;

/**
 * Controls how strictly compile-time type validation is applied.
 *
 * @author sibmaks
 * @since 0.9.0
 */
public enum TemplateTypeValidationMode {
    /** Rejects templates when static type validation finds an incompatible expression. */
    STRICT,

    /** Allows compilation to continue when a type cannot be validated statically. */
    SOFT
}
