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
public class FormatStringTemplateFunction implements TemplateFunction<String> {
    private final Locale locale;

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("format: at least 1 argument required");
        }
        var format = (String) args.get(0);
        var arguments = new Object[args.size()];
        for (int i = 1; i < args.size(); i++) {
            arguments[i - 1] = args.get(i);
        }
        arguments[arguments.length - 1] = pipeArg;
        return String.format(locale, format, arguments);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.size() < 2) {
            throw new TemplateEvalException("format: at least 2 arguments required");
        }
        var format = (String) args.get(0);
        var arguments = new Object[args.size() - 1];
        for (int i = 1; i < args.size(); i++) {
            arguments[i - 1] = args.get(i);
        }
        return String.format(locale, format, arguments);
    }

    @Override
    public String getName() {
        return "format";
    }
}
