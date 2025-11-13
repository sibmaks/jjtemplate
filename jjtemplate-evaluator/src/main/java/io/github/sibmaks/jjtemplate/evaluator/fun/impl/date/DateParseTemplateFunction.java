package io.github.sibmaks.jjtemplate.evaluator.fun.impl.date;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Template function that parses a date string into {@link LocalDate}
 * using a provided format pattern.
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class DateParseTemplateFunction implements TemplateFunction<LocalDate> {

    private LocalDate parseDate(String format, String date) {
        var formatter = DateTimeFormatter.ofPattern(format);
        try {
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw fail("invalid date string: " + date, e);
        }
    }

    @Override
    public LocalDate invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        var format = (String) args.get(0);
        var date = (String) pipeArg;
        return parseDate(format, date);
    }

    @Override
    public LocalDate invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        var format = (String) args.get(0);
        var date = (String) args.get(1);
        return parseDate(format, date);
    }

    @Override
    public String getNamespace() {
        return "date";
    }

    @Override
    public String getName() {
        return "parse";
    }
}
