package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author sibmaks
 */
public class ParseDateTemplateFunction implements TemplateFunction {
    private static String getDate(List<ExpressionValue> args, ExpressionValue pipeArg) {
        if (args.size() == 1) {
            return (String) pipeArg.getValue();
        }
        return (String) args.get(1).getValue();
    }

    @Override
    public ExpressionValue invoke(List<ExpressionValue> args, ExpressionValue pipeArg) {
        var format = (String) args.get(0).getValue();
        var date = getDate(args, pipeArg);
        return ExpressionValue.of(parseDate(format, date));
    }

    private LocalDate parseDate(String format, String date) {
        var formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(date, formatter);
    }

    @Override
    public String getName() {
        return "parseDate";
    }
}
