package io.github.sibmaks.jjtemplate.evaluator.fun.impl;

import io.github.sibmaks.jjtemplate.evaluator.TemplateEvalException;
import io.github.sibmaks.jjtemplate.evaluator.fun.TemplateFunction;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class FormatDateTemplateFunction implements TemplateFunction<String> {

    private static String formatDate(Locale locale, String format, Object date) {
        if (date instanceof TemporalAccessor) {
            var formatter = DateTimeFormatter.ofPattern(format, locale);
            return formatter.format((TemporalAccessor) date);
        }
        if (date instanceof Date) {
            var formatter = new SimpleDateFormat(format, locale);
            return formatter.format((Date) date);
        }
        throw new TemplateEvalException("Cannot convert " + date + " to TemporalAccessor");
    }

    @Override
    public String invoke(List<Object> args, Object pipeArg) {
        if (args.isEmpty()) {
            throw new TemplateEvalException("formatDate: at least 1 argument required");
        }
        var locale = Locale.getDefault();
        String format;
        if (args.get(0) instanceof Locale) {
            locale = (Locale) args.get(0);
            if (args.size() < 2) {
                throw new TemplateEvalException("formatDate: at least 2 arguments required");
            }
            format = (String) args.get(1);
        } else {
            if (args.size() != 1) {
                throw new TemplateEvalException("formatDate: 1 argument required");
            }
            format = (String) args.get(0);
        }
        return formatDate(locale, format, pipeArg);
    }

    @Override
    public String invoke(List<Object> args) {
        if (args.size() < 2) {
            throw new TemplateEvalException("formatDate: at least 2 arguments required");
        }
        var locale = Locale.getDefault();
        String format;
        Object date;
        if (args.get(0) instanceof Locale) {
            locale = (Locale) args.get(0);
            if (args.size() < 3) {
                throw new TemplateEvalException("formatDate: at least 3 arguments required");
            }
            format = (String) args.get(1);
            date = args.get(2);
        } else {
            format = (String) args.get(0);
            date = args.get(1);
        }
        return formatDate(locale, format, date);
    }

    @Override
    public String getName() {
        return "formatDate";
    }
}
