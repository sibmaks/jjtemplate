package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

/**
 *
 * @author sibmaks
 */
public class FormatDateTemplateFunction implements TemplateFunction {
    private static Object getDate(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if (args.size() == 1) {
            return pipeArg.getValue();
        }
        return args.get(1).getValue();
    }

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var format = (String) args.get(0).getValue();
        var date = getDate(args, pipeArg);
        return ExpressionValue.of(formatDate(format, date));
    }

    private String formatDate(String format, Object date) {
        if (date instanceof TemporalAccessor) {
            var formatter = DateTimeFormatter.ofPattern(format);
            var temporalAccessor = (TemporalAccessor) date;
            return formatter.format(temporalAccessor);
        }
        if (date instanceof Date) {
            var formatter = new SimpleDateFormat(format);
            return formatter.format((Date) date);
        }
        throw new TemplateEvalException("Cannot convert " + date + " to TemporalAccessor");
    }

    @Override
    public String getName() {
        return "formatDate";
    }
}
