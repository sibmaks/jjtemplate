package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class ParseDateTimeTemplateFunction implements TemplateFunction<LocalDateTime> {

    private static LocalDateTime parseDate(String format, String date) {
        var formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(date, formatter);
    }

    @Override
    public LocalDateTime invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw new TemplateEvalException("parseDateTime: 1 argument required");
        }
        var format = (String) args.get(0);
        var date = (String) pipeArg;
        return parseDate(format, date);
    }

    @Override
    public LocalDateTime invoke(List<Object> args) {
        if (args.size() != 2) {
            throw new TemplateEvalException("parseDateTime: 2 arguments required");
        }
        var format = (String) args.get(0);
        var date = (String) args.get(1);
        return parseDate(format, date);
    }

    @Override
    public String getName() {
        return "parseDateTime";
    }
}
