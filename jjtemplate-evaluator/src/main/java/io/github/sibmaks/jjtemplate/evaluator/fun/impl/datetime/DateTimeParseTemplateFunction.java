package io.github.sibmaks.jjtemplate.evaluator.fun.impl.datetime;

import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Template function that parses a date-time string into {@link LocalDateTime}
 * using a provided format pattern.
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class DateTimeParseTemplateFunction implements TemplateFunction<LocalDateTime> {

    private LocalDateTime parseDate(String format, String date) {
        var formatter = DateTimeFormatter.ofPattern(format);
        try {
            return LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw fail("invalid date time string: " + date, e);
        }
    }

    @Override
    public LocalDateTime invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw fail("1 argument required");
        }
        var format = (String) args.get(0);
        var date = (String) pipeArg;
        return parseDate(format, date);
    }

    @Override
    public LocalDateTime invoke(List<Object> args) {
        if (args.size() != 2) {
            throw fail("2 arguments required");
        }
        var format = (String) args.get(0);
        var date = (String) args.get(1);
        return parseDate(format, date);
    }

    @Override
    public String getNamespace() {
        return "datetime";
    }

    @Override
    public String getName() {
        return "parse";
    }
}
