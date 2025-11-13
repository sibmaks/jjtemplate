package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Locale;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class StringUpperTemplateFunction implements TemplateFunction<String> {

    private String upper(Locale locale, Object value) {
        if (value == null) {
            return null;
        }
        var string = String.valueOf(value);
        return string.toUpperCase(locale);
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        var locale = Locale.getDefault();
        if (args.size() == 1) {
            locale = (Locale) args.get(0);
        } else if (args.size() > 1) {
            throw new TemplateEvalException("upper: too much arguments passed");
        }
        return upper(locale, pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("upper: at least 1 argument required");
        }
        var locale = Locale.getDefault();
        var value = args.get(0);
        if (args.size() == 2) {
            locale = (Locale) args.get(0);
            value = args.get(1);
        }
        if (args.size() > 2) {
            throw new TemplateEvalException("upper: too much arguments passed");
        }
        return upper(locale, value);
    }

    @Override
    public String getName() {
        return "upper";
    }
}
