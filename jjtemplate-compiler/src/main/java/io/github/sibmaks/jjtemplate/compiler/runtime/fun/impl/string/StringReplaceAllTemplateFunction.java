package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

/**
 * Template function that replaces all occurrences of a target substring
 * within a string.
 * <p>
 * Applies global replacement semantics equivalent to
 * {@link String#replaceAll(String, String)}.
 * </p>
 *
 * <p>
 * This function replaces <em>all</em> matching occurrences and does not
 * interpret the target value as a regular expression.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class StringReplaceAllTemplateFunction extends AbstractStringReplaceTemplateFunction {

    @Override
    protected String replace(String value, String target, String replacement) {
        var string = String.valueOf(value);
        return string.replaceAll(target, replacement);
    }

    @Override
    public String getName() {
        return "replaceAll";
    }
}
