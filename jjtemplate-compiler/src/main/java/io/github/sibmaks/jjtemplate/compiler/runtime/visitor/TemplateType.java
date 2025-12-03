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
     * Plain literal text
     */
    STATIC,
    /**
     * Value interpolation or general expression
     */
    EXPRESSION,
    /**
     * Conditional interpolation such as {@code {{? expr }}}
     */
    CONDITION,
    /**
     * Spread operation used to merge objects or lists
     */
    SPREAD,
    SWITCH,
    SWITCH_ELSE,
    RANGE
}
