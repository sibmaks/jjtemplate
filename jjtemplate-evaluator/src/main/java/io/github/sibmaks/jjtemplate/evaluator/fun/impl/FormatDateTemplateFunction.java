package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class FormatDateTemplateFunction implements TemplateFunction<String> {
    private static String formatDate(String format, Object date) {
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
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.size() != 1) {
            throw new TemplateEvalException("formatDate: 1 argument required");
        }
        var format = (String) args.get(0);
        var date = (String) pipeArg;
        return formatDate(format, date);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.size() != 2) {
            throw new TemplateEvalException("formatDate: 2 arguments required");
        }
        var format = (String) args.get(0);
        var date = (String) args.get(1);
        return formatDate(format, date);
    }

    @Override
    public String getName() {
        return "formatDate";
    }
}
