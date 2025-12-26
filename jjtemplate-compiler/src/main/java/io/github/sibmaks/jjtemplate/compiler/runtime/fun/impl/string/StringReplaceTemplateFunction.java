package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

/**
 * Template function that replaces the first occurrence of a target substring
 * within a string.
 * <p>
 * Applies replacement semantics equivalent to {@link String#replace(CharSequence, CharSequence)}.
 * </p>
 *
 * <p>
 * Only the first matching occurrence is replaced.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringReplaceTemplateFunction extends AbstractStringReplaceTemplateFunction {

    @Override
    protected String replace(String value, String target, String replacement) {
        var string = String.valueOf(value);
        return string.replace(target, replacement);
    }

    @Override
    public String getName() {
        return "replace";
    }
}
