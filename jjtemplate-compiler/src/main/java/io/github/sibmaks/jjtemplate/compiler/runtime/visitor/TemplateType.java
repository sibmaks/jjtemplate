package io.github.sibmaks.jjtemplate.compiler.runtime.visitor;

/**
 * Describes the semantic category of a template fragment.
 * <p>
 * The compiler assigns a {@link TemplateType} to each part of the template
 * based on syntactic analysis performed by {@code TemplateTypeInferenceVisitor}.
 * These categories determine how the fragment will be transformed
 * into an AST node during compilation.
 * </p>
 *
 * @author sibmaks
 * @since 0.5.0
 */
public enum TemplateType {
    /**
     * Constant literal template.
     * <p>
     * Represents a static value embedded directly into the template
     * and returned as-is during evaluation.
     * </p>
     */
    CONSTANT,

    /**
     * Value interpolation or general expression template.
     * <p>
     * Represents an arbitrary expression whose value is evaluated
     * at runtime and inserted into the result.
     * </p>
     */
    EXPRESSION,

    /**
     * Conditional interpolation template.
     * <p>
     * Evaluates the underlying expression and includes its result
     * only if the condition is satisfied.
     * </p>
     *
     * <p>
     * Typically corresponds to conditional constructs such as
     * {@code {{? expr }}}.
     * </p>
     */
    CONDITION,

    /**
     * Spread operation template.
     * <p>
     * Expands the contents of another object or list into the
     * surrounding structure during evaluation.
     * </p>
     */
    SPREAD,

    /**
     * Switch template.
     * <p>
     * Selects one of several alternative branches based on the
     * evaluated switch condition.
     * </p>
     */
    SWITCH,

    /**
     * Switch {@code else} template.
     * <p>
     * Represents an unconditional fallback branch of a switch
     * expression, evaluated when no other case matches.
     * </p>
     */
    SWITCH_ELSE,

    /**
     * Range template.
     * <p>
     * Iterates over a collection or array and produces a sequence
     * of values by evaluating a body expression for each element.
     * </p>
     */
    RANGE
}
