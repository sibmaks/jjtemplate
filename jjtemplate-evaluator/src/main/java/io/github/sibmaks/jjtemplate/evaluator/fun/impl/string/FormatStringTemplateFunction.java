package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Locale;

/**
 *
 * @author sibmaks
 */
@AllArgsConstructor
public class FormatStringTemplateFunction implements TemplateFunction {
    private final Locale locale;

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var format = (String) args.get(0).getValue();
        var argsCount = args.size() - 1 + (pipeArg.isEmpty() ? 0 : 1);
        var arguments = new Object[argsCount];
        for (int i = 1; i < args.size(); i++) {
            arguments[i - 1] = args.get(i).getValue();
        }
        if (!pipeArg.isEmpty()) {
            arguments[argsCount - 1] = pipeArg.getValue();
        }

        var formatted = String.format(locale, format, arguments);
        return ExpressionValue.of(formatted);
    }

    @Override
    public String getName() {
        return "format";
    }
}
