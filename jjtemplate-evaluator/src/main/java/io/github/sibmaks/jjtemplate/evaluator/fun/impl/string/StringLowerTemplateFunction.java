package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Locale;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@AllArgsConstructor
public class StringLowerTemplateFunction implements TemplateFunction<String> {
    private final Locale locale;

    private String lower(Object value) {
        if (value == null) {
            return null;
        }
        var string = String.valueOf(value);
        return string.toLowerCase(locale);
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("lower: too much arguments passed");
        }
        return lower(pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("lower: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("lower: too much arguments passed");
        }
        return lower(args.get(0));
    }

    @Override
    public String getName() {
        return "lower";
    }
}
