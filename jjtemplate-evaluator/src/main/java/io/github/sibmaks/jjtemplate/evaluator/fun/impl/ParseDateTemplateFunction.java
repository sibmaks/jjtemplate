package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class ParseDateTemplateFunction implements TemplateFunction<LocalDate> {

    private static LocalDate parseDate(String format, String date) {
        var formatter = DateTimeFormatter.ofPattern(format);
        try {
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new TemplateEvalException("Invalid date string: " + date, e);
        }
    }

    @Override
    public LocalDate invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw new TemplateEvalException("parseDate: 1 argument required");
        }
        var format = (String) args.get(0);
        var date = (String) pipeArg;
        return parseDate(format, date);
    }

    @Override
    public LocalDate invoke(List<Object> args) {
        if (args.size() != 2) {
            throw new TemplateEvalException("parseDate: 2 arguments required");
        }
        var format = (String) args.get(0);
        var date = (String) args.get(1);
        return parseDate(format, date);
    }

    @Override
    public String getName() {
        return "parseDate";
    }
}
