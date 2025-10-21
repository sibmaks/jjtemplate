package io.github.sibmaks.jjtemplate.evaluator.fun.impl.string;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.util.List;
import java.util.Locale;

/**
 *
 * @author sibmaks
 */
public class StringLowerTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var argument = first(args, pipeArg);
        if (argument.isEmpty()) {
            throw new IllegalArgumentException("lower: 1 argument required");
        }
        var value = argument.getValue();
        var stringValue = String.valueOf(value);
        return ExpressionValue.of(stringValue.toLowerCase(Locale.ROOT));
    }

    @Override
    public String getName() {
        return "lower";
    }
}
