package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Locale;

/**
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

}
