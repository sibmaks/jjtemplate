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
public class StringUpperTemplateFunction implements TemplateFunction<String> {
    private final Locale locale;

    private String upper(Object value) {
        if (value == null) {
            return null;
        }
        var string = String.valueOf(value);
        return string.toUpperCase(locale);
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (!args.isEmpty()) {
            throw new TemplateEvalException("upper: too much arguments passed");
        }
        return upper(pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("upper: 1 argument required");
        }
        if (args.size() != 1) {
            throw new TemplateEvalException("upper: too much arguments passed");
        }
        return upper(args.get(0));
    }

    @Override
    public String getName() {
        return "upper";
    }
}
