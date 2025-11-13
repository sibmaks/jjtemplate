package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@AllArgsConstructor
public class FormatStringTemplateFunction implements TemplateFunction<String> {

    private static String format(List<Object> args) {
        var locale = Locale.getDefault();
        String format;
        var firstArg = args.remove(0);
        if (firstArg instanceof Locale) {
            locale = (Locale) firstArg;
            if (args.isEmpty()) {
                throw new TemplateEvalException("format: at least 2 arguments required");
            }
            format = (String) args.remove(0);
        } else {
            format = (String) firstArg;
        }
        var arguments = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            arguments[i] = args.get(i);
        }
        return String.format(locale, format, arguments);
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("format: at least 1 argument required");
        }
        var newArgs = new ArrayList<>(args);
        newArgs.add(pipeArg);
        return format(newArgs);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("format: at least 1 argument required");
        }
        var newArgs = new LinkedList<>(args);
        return format(newArgs);
    }

    @Override
    public String getName() {
        return "format";
    }
}
