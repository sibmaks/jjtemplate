package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;

/**
 *
 * @author sibmaks
 */
public class FormatDateTemplateFunction implements TemplateFunction {
    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var format = (String) args.get(0).getValue();
        var formatter = DateTimeFormatter.ofPattern(format);
        var date = getDate(args, pipeArg);
        return ExpressionValue.of(formatter.format(date));
    }

    private static TemporalAccessor getDate(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if(args.size() == 1) {
            return (TemporalAccessor) pipeArg.getValue();
        }
        return (TemporalAccessor) args.get(1).getValue();
    }

    @Override
    public String getName() {
        return "formatDate";
    }
}
