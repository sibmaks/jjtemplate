package io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl.string;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

import java.util.List;
import java.util.Locale;

/**
 * Template function that converts a string to a specific character case.
 * <p>
 * Applies a case-transformation strategy (for example: upper-case or lower-case)
 * to the input string. The exact transformation behavior is defined by the
 * concrete implementation.
 * </p>
 *
 * <p>
 * This function operates purely on string values and does not modify
 * the evaluation {@link io.github.sibmaks.jjtemplate.compiler.runtime.context.Context}.
 * </p>
 *
 * @author sibmaks
 * @since 0.4.0
 */
public abstract class StringCaseTemplateFunction implements TemplateFunction<String> {

    /**
     * Format string into required case
     *
     * @param locale locale to format string
     * @param value  value to format
     * @return formated string
     */
    protected abstract String toCase(Locale locale, Object value);

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        var locale = Locale.getDefault();
        if (args.size() == 1) {
            locale = (Locale) args.get(0);
        } else if (args.size() > 1) {
            throw fail("too much arguments passed");
        }
        return toCase(locale, pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw fail("at least 1 argument required");
        }
        var locale = Locale.getDefault();
        var value = args.get(0);
        if (args.size() == 2) {
            locale = (Locale) args.get(0);
            value = args.get(1);
        }
        if (args.size() > 2) {
            throw fail("too much arguments passed");
        }
        return toCase(locale, value);
    }

    @Override
    public String getNamespace() {
        return "string";
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

}
